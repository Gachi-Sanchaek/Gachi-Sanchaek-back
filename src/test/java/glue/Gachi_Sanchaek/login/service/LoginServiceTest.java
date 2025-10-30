package glue.Gachi_Sanchaek.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private KakaoLoginService kakaoLoginService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginService loginService;


    @Test
    @DisplayName("기존 유저 로그인 시, 회원가입 없이 토큰을 발급한다")
    void loginWithKakaoCode_ExistingUser_ShouldReturnTokens() {
        // given
        String code = "test-code";
        String kakaoAccessToken = "kakao-access-token";
        String expectedAccessToken = "service-access-token";
        String expectedRefreshToken = "service-refresh-token";

        User existingUser = Mockito.mock(User.class);
        when(existingUser.getId()).thenReturn(1L);
        when(existingUser.getRole()).thenReturn("USER");
        when(existingUser.getNickname()).thenReturn("기존유저");

        KakaoUserInfoResponseDto kakaoInfo = new KakaoUserInfoResponseDto();
        kakaoInfo.id = 987654321L;

        when(kakaoLoginService.getAccessTokenFromKakao(code)).thenReturn(kakaoAccessToken);
        when(kakaoLoginService.getUserInfo(kakaoAccessToken)).thenReturn(kakaoInfo);

        when(userService.findByKakaoId(kakaoInfo.id)).thenReturn(Optional.of(existingUser));

        when(tokenService.createAccessToken(1L, "USER")).thenReturn(expectedAccessToken);
        when(tokenService.createRefreshToken(1L)).thenReturn(expectedRefreshToken);

        // when
        LoginService.AuthResult result = loginService.loginWithKakaoCode(code);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(expectedRefreshToken);

        LoginResponseDto dto = result.getLoginResponseDto();
        assertThat(dto.isNewUser()).isFalse();
        assertThat(dto.getNickname()).isEqualTo("기존유저");

        verify(kakaoLoginService).getAccessTokenFromKakao(code);
        verify(kakaoLoginService).getUserInfo(kakaoAccessToken);
        verify(userService).findByKakaoId(kakaoInfo.id);
        verify(userService, never()).registerInitialUser(any());
        verify(tokenService).createAccessToken(1L, "USER");
        verify(tokenService).createRefreshToken(1L);
    }

    @Test
    @DisplayName("신규 유저 로그인 시, 회원가입 후 토큰을 발급한다")
    void loginWithKakaoCode_NewUser_ShouldRegisterAndReturnTokens() {
        // given: 상황 설정
        String code = "test-code";
        String kakaoAccessToken = "kakao-access-token";
        String expectedAccessToken = "new-access-token";
        String expectedRefreshToken = "new-refresh-token";

        User newUser = Mockito.mock(User.class);
        when(newUser.getId()).thenReturn(2L);
        when(newUser.getRole()).thenReturn("USER");
        when(newUser.getNickname()).thenReturn("신규유저");

        KakaoUserInfoResponseDto kakaoInfo = new KakaoUserInfoResponseDto();
        KakaoUserInfoResponseDto.KakaoAccount mockAccount = kakaoInfo.new KakaoAccount();
        mockAccount.email = "test@email.com";
        kakaoInfo.id = 987654321L;
        kakaoInfo.kakaoAccount = mockAccount;
        kakaoInfo.properties = new HashMap<>();

        when(kakaoLoginService.getAccessTokenFromKakao(code)).thenReturn(kakaoAccessToken);
        when(kakaoLoginService.getUserInfo(kakaoAccessToken)).thenReturn(kakaoInfo);
        when(userService.findByKakaoId(kakaoInfo.id)).thenReturn(Optional.empty());
        when(userService.registerInitialUser(any(UserJoinDto.class))).thenReturn(newUser);
        when(tokenService.createAccessToken(2L, "USER")).thenReturn(expectedAccessToken);
        when(tokenService.createRefreshToken(2L)).thenReturn(expectedRefreshToken);

        // when
        LoginService.AuthResult result = loginService.loginWithKakaoCode(code);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(expectedRefreshToken);

        LoginResponseDto dto = result.getLoginResponseDto();
        assertThat(dto.isNewUser()).isTrue();
        assertThat(dto.getNickname()).isEqualTo("신규유저");

        verify(userService).findByKakaoId(kakaoInfo.id);
        verify(userService).registerInitialUser(any(UserJoinDto.class));
        verify(tokenService).createAccessToken(2L, "USER");
        verify(tokenService).createRefreshToken(2L);
    }

    @Test
    @DisplayName("토큰 재발급 시 TokenService에 위임한다")
    void reissueAccessToken_ShouldDelegateToTokenService() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String expectedNewAccessToken = "new-access-token";

        when(tokenService.reissueAccessToken(oldRefreshToken)).thenReturn(expectedNewAccessToken);

        // when
        String result = loginService.reissueAccessToken(oldRefreshToken);

        // then
        assertThat(result).isEqualTo(expectedNewAccessToken);
        verify(tokenService).reissueAccessToken(oldRefreshToken);
    }
}

