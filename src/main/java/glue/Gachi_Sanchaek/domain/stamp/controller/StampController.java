package glue.Gachi_Sanchaek.domain.stamp.controller;

import glue.Gachi_Sanchaek.common.docs.SecureOperation;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.domain.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.domain.stamp.service.StampService;
import glue.Gachi_Sanchaek.common.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stamp API", description = "스탬프 조회 관련 API")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/stamps")
public class StampController {

    private final StampService stampService;

    @SecureOperation(
            summary = "전체 스탬프 목록 조회",
            description = "시스템의 모든 스탬프 목록을 조회하며, 현재 로그인한 유저의 활성화 여부(보유 포인트 >= 스탬프 가격)를 포함하여 반환합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<StampResponseDto>>> findAllStamps(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<StampResponseDto> result = stampService.getAllStampsWithUserStatus(userId);
        return ApiResponse.ok(result);
    }

}
