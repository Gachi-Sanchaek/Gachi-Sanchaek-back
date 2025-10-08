package glue.Gachi_Sanchaek.userStamp.controller;


import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.userStamp.dto.UserStampResponseDto;
import glue.Gachi_Sanchaek.userStamp.service.UserStampService;
import glue.Gachi_Sanchaek.util.ApiResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/user-stamp")
public class UserStampController {
    private final UserStampService userStampService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserStampResponseDto>>> findAllByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long id = Long.valueOf(userDetails.getUsername());
        List<UserStampResponseDto> result = userStampService.findAllByUserId(id).stream().map(UserStampResponseDto::new)
                .toList();
        return ApiResponse.ok(result);
    }

}
