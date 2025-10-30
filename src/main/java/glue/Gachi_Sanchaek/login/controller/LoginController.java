package glue.Gachi_Sanchaek.login.controller;


import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.service.LoginService.AuthResult;
import glue.Gachi_Sanchaek.login.service.TokenService;
import glue.Gachi_Sanchaek.login.service.LoginService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private final LoginService loginService;

    @GetMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> callback(@RequestParam("code") String code) {

        AuthResult authResult = loginService.loginWithKakaoCode(code);

        return ApiResponse.okWithAuthHeader(
                authResult.getLoginResponseDto(),
                authResult.getAccessToken(),
                authResult.getRefreshToken());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token is missing.");
        }

        String newAccessToken = loginService.reissueAccessToken(refreshToken);
        return ApiResponse.okWithAuthHeader(null, newAccessToken);
    }
}
