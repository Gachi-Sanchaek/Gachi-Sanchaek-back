package glue.Gachi_Sanchaek.walkRecommendation.controller;

import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.walkRecommendation.dto.SaveWalkRouteResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRecommendationGroupResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRouteSelectionRequest;
import glue.Gachi_Sanchaek.walkRecommendation.service.WalkRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static glue.Gachi_Sanchaek.util.ApiResponse.ok;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes")
public class WalkRecommendationController {
    private final WalkRecommendationService walkRecommendationService;

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<WalkRecommendationGroupResponse>> getRecommend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long orgId,
            @RequestParam int minutes,
            @RequestParam double currentLat,
            @RequestParam double currentLng){
        WalkRecommendationGroupResponse recommendedRoutes = walkRecommendationService.recommendRoutes(orgId, minutes,currentLat,currentLng);
        return ok(recommendedRoutes);
    }

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<SaveWalkRouteResponse>> saveRoute(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody WalkRouteSelectionRequest req){
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 유효하지 않거나 만료되었습니다.");
        }
        Long userId = userDetails.getUserId();
        SaveWalkRouteResponse saved = walkRecommendationService.saveSelectedRoute(userId,req);
        return ok(saved);
    }



}
