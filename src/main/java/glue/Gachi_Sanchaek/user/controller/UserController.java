package glue.Gachi_Sanchaek.user.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.user.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.user.dto.NicknameValidateResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserResponseDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameValidateResponseDto>> checkNickname(
            @RequestParam("nickname") String nickname){
        boolean isAvailable = userService.isAvailableNickname(nickname);
        return ApiResponse.ok(new NicknameValidateResponseDto(nickname, isAvailable));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Void>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserJoinRequestDto requestDto){
        User user = userService.update(Long.valueOf(userDetails.getUsername()), requestDto);
        return ApiResponse.ok(null);
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        User user = userService.findById(Long.valueOf(userDetails.getUsername()));
        return ApiResponse.ok(new UserResponseDto(user));
    }
}
