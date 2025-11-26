package glue.Gachi_Sanchaek.login.controller;


import glue.Gachi_Sanchaek.docs.SecureOperation;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.service.LoginService;
import glue.Gachi_Sanchaek.login.service.LoginService.AuthResult;
import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Login API", description = "로그인 및 인증/인가 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class LoginController {
    private final LoginService loginService;

    @Operation(
            summary = "카카오 로그인/회원가입 코드 검증",
            description = "카카오 인가 코드를 받아 로그인 또는 회원가입을 처리하고 JWT 토큰을 발급합니다. ")
    @GetMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> callback(
            @Parameter(description = "카카오 서버로부터 받은 인가 코드", required = true)
            @RequestParam("code") String code) {

        AuthResult authResult = loginService.loginWithKakaoCode(code);

        return ApiResponse.okWithAuthHeader(
                authResult.getLoginResponseDto(),
                authResult.getAccessToken(),
                authResult.getRefreshToken());
    }

    @SecureOperation(
            summary = "액세스 토큰 재발급",
            description = "HttpOnly 쿠키로 전달된 리프레시 토큰을 검증하여 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshAccessToken(
            @Parameter(description = "HttpOnly 쿠키로 전달되는 리프레시 토큰 (Swagger UI에서 직접 테스트 시에는 값이 전달되지 않을 수 있음)",
                    hidden = true)
            @CookieValue("refreshToken") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token is missing.");
        }

        String newAccessToken = loginService.reissueAccessToken(refreshToken);
        return ApiResponse.okWithAuthHeader(null, newAccessToken);
    }
}
