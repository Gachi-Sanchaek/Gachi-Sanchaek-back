package glue.Gachi_Sanchaek.pointLog.controller;

import glue.Gachi_Sanchaek.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pointLog")
@RequiredArgsConstructor
public class PointLogController {
    private final PointLogService pointLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PointLogResponseDto>>> getAllPointLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PointLogResponseDto> pointLogs = pointLogService.findByUserId(userDetails.getUserId());
        return ApiResponse.ok(pointLogs);
    }
}
