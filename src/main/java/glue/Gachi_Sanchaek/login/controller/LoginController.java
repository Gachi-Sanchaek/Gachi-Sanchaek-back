package glue.Gachi_Sanchaek.login.controller;


import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.user.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.login.service.KakaoLoginService;
import glue.Gachi_Sanchaek.login.service.LoginService;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<ApiResponse<LoginResponseDto>> callback(@RequestParam("code") String code) {
        String kakaoAccessToken = kakaoLoginService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoLoginService.getUserInfo(kakaoAccessToken); //카카오 유저 정보

        User user = loginService.findByKakaoId(userInfo.getId());
        boolean isNewUser = false;
        if(user == null){ //신규 회원
            isNewUser = true;
            user = loginService.joinProcess(new UserJoinDto(userInfo),"USER");
        }

        LoginResponseDto loginResponseDto = new LoginResponseDto(isNewUser, user.getNickname());

        String accessToken = loginService.createToken(user.getId(), user.getRole());
        System.out.println("accessToken = " + accessToken);
        String refreshToken = loginService.createRefreshToken(user.getId());
        return ApiResponse.okWithAuthHeader(loginResponseDto, accessToken, refreshToken);
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
