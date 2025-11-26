package glue.Gachi_Sanchaek.login.service;

import glue.Gachi_Sanchaek.common.redis.service.RedisService;
import glue.Gachi_Sanchaek.common.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.domain.login.service.TokenService;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RedisService redisService;


    @Mock
    private UserService userService;


    @InjectMocks
    private TokenService tokenService;

    private final String REFRESH_TOKEN_PREFIX = "refresh:";

    @Captor
    private ArgumentCaptor<String> redisKeyCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "accessExpirationSecond", 3600000L); // 1시간
        ReflectionTestUtils.setField(tokenService, "refreshExpirationSecond", 86400000L); // 24시간

    }

    @Test
    @DisplayName("액세스 토큰 생성 시 JWTUtil을 올바른 만료 시간으로 호출한다")
    void createAccessToken_Success() {
        // given
        Long userId = 1L;
        String role = "ROLE_USER";
        String expectedToken = "mock-access-token";

        when(jwtUtil.createJwt(userId, role, 3600000L)).thenReturn(expectedToken);


        // when
        String accessToken = tokenService.createAccessToken(userId, role);


        // then
        assertThat(accessToken).isEqualTo(expectedToken);
        verify(jwtUtil).createJwt(userId, role, 3600000L);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 시 Redis에 올바른 Key-Value와 만료 시간으로 저장한다")
    void createRefreshToken_Success() {
        // given
        Long userId = 1L;
        Long expectedDurationMillis = 86400000L;


        // when
        String refreshToken = tokenService.createRefreshToken(userId);


        // then
        assertThat(refreshToken).isNotNull();

        verify(redisService).save(
                redisKeyCaptor.capture(),
                eq(String.valueOf(userId)),
                eq(expectedDurationMillis)
        );

        String capturedKey = redisKeyCaptor.getValue();
        assertThat(capturedKey).startsWith(REFRESH_TOKEN_PREFIX);
        assertThat(capturedKey).endsWith(refreshToken);
        assertThat(capturedKey).isEqualTo(REFRESH_TOKEN_PREFIX + refreshToken);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰 검증 시 userId를 Optional로 반환한다")
    void validateRefreshToken_Success() {
        // given
        String token = "valid-token";
        String expectedUserId = "1";
        String key = REFRESH_TOKEN_PREFIX + token;

        when(redisService.get(key)).thenReturn(expectedUserId);

        // when
        Optional<String> result = tokenService.validateRefreshToken(token);

        // then
        assertThat(result).isPresent().contains(expectedUserId);
        verify(redisService).get(key);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰 검증 시 빈 Optional을 반환한다")
    void validateRefreshToken_Failure() {
        // given
        String token = "invalid-token";
        String key = REFRESH_TOKEN_PREFIX + token;

        when(redisService.get(key)).thenReturn(null);

        // when
        Optional<String> result = tokenService.validateRefreshToken(token);

        // then
        assertThat(result).isEmpty();
        verify(redisService).get(key);
    }

    @Test
    @DisplayName("토큰 재발급 시 유효한 토큰이면 새 액세스 토큰을 반환한다")
    void reissueAccessToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = "1";
        String newAccessToken = "new-access-token";

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getRole()).thenReturn("ROLE_USER");

        when(redisService.get(key)).thenReturn(userId);
        when(userService.findById(1L)).thenReturn(mockUser);
        when(jwtUtil.createJwt(1L, "ROLE_USER", 3600000L)).thenReturn(newAccessToken);

        // when
        String result = tokenService.reissueAccessToken(refreshToken);

        // then
        assertThat(result).isEqualTo(newAccessToken);
        verify(redisService).get(key);
        verify(userService).findById(1L);
        verify(jwtUtil).createJwt(1L, "ROLE_USER", 3600000L);
    }

    @Test
    @DisplayName("토큰 재발급 시 유효하지 않은 토큰이면 예외를 던진다")
    void reissueAccessToken_InvalidToken_ThrowsException() {
        // given
        String refreshToken = "invalid-token";
        String key = REFRESH_TOKEN_PREFIX + refreshToken;

        when(redisService.get(key)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> tokenService.reissueAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token is invalid or expired.");

        verify(redisService).get(key);
        verify(userService, never()).findById(anyLong());
        verify(jwtUtil, never()).createJwt(anyLong(), anyString(), anyLong());
    }
}