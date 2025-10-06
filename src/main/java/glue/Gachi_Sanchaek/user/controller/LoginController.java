package glue.Gachi_Sanchaek.user.controller;


import glue.Gachi_Sanchaek.user.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.dto.UserResponseDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.KakaoLoginService;
import glue.Gachi_Sanchaek.user.service.LoginService;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.AuthenticationException;
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
    private final UserService userService;

    @GetMapping("/kakao/login")
    public ResponseEntity<ApiResponse<Void>> callback(@RequestParam("code") String code) {
        String kakaoAccessToken = kakaoLoginService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoLoginService.getUserInfo(kakaoAccessToken); //카카오 유저 정보

        User user = loginService.joinProcess(new UserJoinDto(userInfo), "USER");
        String accessToken = loginService.createToken(user.getId(), user.getRole());
        String refreshToken = loginService.createRefreshToken(user.getId());
        return ApiResponse.okWithAuthHeader(null, accessToken, refreshToken);
    }

//    @PostMapping("/refresh")
//    public ResponseEntity<ApiResponse<Void>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response)
//            throws AuthenticationException {
//        String refreshToken = null;
//        if (request.getCookies() != null) {
//            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
//                if ("refreshToken".equals(cookie.getName())) {
//                    refreshToken = cookie.getValue();
//                    break;
//                }
//            }
//        }
//
//        if (refreshToken == null || refreshToken.isEmpty()) {
//            throw new AuthenticationException("refreshToken is empty");
//        }
//
//        String userId = loginService.validateRefreshToken(refreshToken)
//                .orElseThrow(() -> new AuthenticationException("Refresh token is invalid or expired."));
//
//        UserResponseDto userResponseDto = userService.findById(Long.valueOf(userId));
//
//        String newAccessToken = loginService.createToken(Long.valueOf(userId), userResponseDto.getRole());
//
//        return ApiResponse.okWithAuthHeader(null, newAccessToken);
//    }
}
