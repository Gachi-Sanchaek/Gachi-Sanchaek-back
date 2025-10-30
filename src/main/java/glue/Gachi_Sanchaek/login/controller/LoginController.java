package glue.Gachi_Sanchaek.login.controller;


import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.login.service.TokenService;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.login.service.KakaoLoginService;
import glue.Gachi_Sanchaek.login.service.LoginService;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import javax.security.sasl.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final KakaoLoginService kakaoLoginService;
    private final LoginService loginService;
    private final TokenService tokenService;
    private final UserService userService;

    @GetMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> callback(@RequestParam("code") String code) {
        String kakaoAccessToken = kakaoLoginService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoLoginService.getUserInfo(kakaoAccessToken); //카카오 유저 정보

        LoginResponseDto loginResponseDto = loginService.kakaoLogin(userInfo);
        User user = loginResponseDto.getUser();

        String accessToken = tokenService.createToken(user.getId(), user.getRole());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        System.out.println("accessToken = " + accessToken);
        return ApiResponse.okWithAuthHeader(loginResponseDto, accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthenticationException("refreshToken is empty");
        }

        String userId = tokenService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Refresh token is invalid or expired."));

        User user = userService.findById(Long.valueOf(userId));

        String newAccessToken = tokenService.createToken(Long.valueOf(userId), user.getRole());

        return ApiResponse.okWithAuthHeader(null, newAccessToken);
    }
}
