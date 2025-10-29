package glue.Gachi_Sanchaek.user.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.user.dto.NicknameValidateResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserUpdateRequestDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> join(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserJoinRequestDto requestDto){
        User user = userService.join(userDetails.getUserId(), requestDto);
        return ApiResponse.created(new UserResponseDto(user));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        User user = userService.findById(userDetails.getUserId());
        return ApiResponse.ok(new UserResponseDto(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequestDto requestDto
    ){
        User user = userService.update(userDetails.getUserId(),requestDto);
        return ApiResponse.ok(new UserResponseDto(user));
    }
}
