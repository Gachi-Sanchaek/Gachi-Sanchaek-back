package glue.Gachi_Sanchaek.domain.pointLog.controller;

import glue.Gachi_Sanchaek.common.docs.SecureOperation;
import glue.Gachi_Sanchaek.domain.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.domain.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.common.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PointLog API", description = "포인트 로그 관련 API")
@RestController
@RequestMapping("/api/v1/pointLog")
@RequiredArgsConstructor
public class PointLogController {
    private final PointLogService pointLogService;

    @SecureOperation(summary = "내 포인트 로그 전체 조회", description = "현재 로그인한 사용자의 모든 포인트 적립/사용 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PointLogResponseDto>>> getAllPointLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PointLogResponseDto> pointLogs = pointLogService.findByUserId(userDetails.getUserId());
        return ApiResponse.ok(pointLogs);
    }
}
