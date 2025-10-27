package glue.Gachi_Sanchaek.walkRecommendation.controller;

import glue.Gachi_Sanchaek.common.ApiResponse;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRecommendationGroupResponse;
import glue.Gachi_Sanchaek.walkRecommendation.service.WalkRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes")
public class WalkRecommendationController {
    private final WalkRecommendationService walkRecommendationService;

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<WalkRecommendationGroupResponse>> getRecommend(
            @AuthenticationPrincipal User user,
            @RequestParam Long orgId,
            @RequestParam int minutes){

        return ResponseEntity.ok(ApiResponse.onSuccess(walkRecommendationService.recommendRoutes(orgId, minutes)));
    }


}
