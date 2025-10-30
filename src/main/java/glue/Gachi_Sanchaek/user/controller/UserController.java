package glue.Gachi_Sanchaek.user.controller;

import glue.Gachi_Sanchaek.docs.SecureOperation;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.user.dto.NicknameValidateResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserResponseDto;
import glue.Gachi_Sanchaek.user.dto.UserUpdateRequestDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @SecureOperation(summary = "닉네임 중복 검사", description = "사용자가 입력한 닉네임의 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameValidateResponseDto>> checkNickname(
            @RequestParam("nickname") String nickname
    ){
        boolean isAvailable = userService.isAvailableNickname(nickname);
        return ApiResponse.ok(new NicknameValidateResponseDto(nickname, isAvailable));
    }

    @SecureOperation(summary = "신규 회원 추가 정보 입력 (회원가입)", description = "카카오 로그인 후, 신규 회원의 닉네임과 성별을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> join(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserJoinRequestDto requestDto
    ){
        User user = userService.completeRegistration(userDetails.getUserId(), requestDto);
        return ApiResponse.created(new UserResponseDto(user));
    }

    @SecureOperation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        User user = userService.findById(userDetails.getUserId());
        return ApiResponse.ok(new UserResponseDto(user));
    }

    @SecureOperation(summary = "내 정보 수정", description = "현재 로그인된 사용자의 닉네임 또는 프로필 이미지를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserUpdateRequestDto requestDto
    ){
        User user = userService.update(userDetails.getUserId(),requestDto);
        return ApiResponse.ok(new UserResponseDto(user));
    }
}
