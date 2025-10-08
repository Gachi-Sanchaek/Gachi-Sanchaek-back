package glue.Gachi_Sanchaek.user.controller;

import glue.Gachi_Sanchaek.user.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.user.dto.NicknameValidateResponseDto;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameValidateResponseDto>> checkNickname(String nickname){
        boolean isAvailable = userService.isAvailableNickname(nickname);
        return ApiResponse.ok(new NicknameValidateResponseDto(nickname, isAvailable));
    }
}
