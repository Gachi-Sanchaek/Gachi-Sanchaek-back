package glue.Gachi_Sanchaek.login.service;

import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;
    private final TokenService tokenService;

    @Transactional
    public AuthResult loginWithKakaoCode(String code) {

        String kakaoAccessToken = kakaoLoginService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoLoginService.getUserInfo(kakaoAccessToken);

        LoginResult loginResult = kakaoLogin(userInfo);
        User user = loginResult.getUser();

        String accessToken = tokenService.createAccessToken(user.getId(), user.getRole());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        System.out.println("accessToken = " + accessToken);
        LoginResponseDto loginResponseDto = new LoginResponseDto(loginResult.isNewUser(), user);

        return new AuthResult(loginResponseDto, accessToken, refreshToken);
    }

    private LoginResult kakaoLogin(KakaoUserInfoResponseDto userInfo) {
        Optional<User> userOpt = userService.findByKakaoId(userInfo.getId());

        if (userOpt.isEmpty() || userOpt.get().isDeleted()) {
            User newUser = userService.registerInitialUser(new UserJoinDto(userInfo));
            return new LoginResult(newUser, true);
        }

        User user = userOpt.get();
        if(Objects.equals(user.getGender(), "NONE")){
            return new LoginResult(user, true);
        }

        return new LoginResult(userOpt.get(), false);
    }

    public String reissueAccessToken(String refreshToken) {
        return tokenService.reissueAccessToken(refreshToken);
    }

    @Getter
    @RequiredArgsConstructor
    public static class AuthResult {
        private final LoginResponseDto loginResponseDto;
        private final String accessToken;
        private final String refreshToken;
    }

    @Getter
    @RequiredArgsConstructor
    private static class LoginResult {
        private final User user;
        private final boolean isNewUser;
    }
}