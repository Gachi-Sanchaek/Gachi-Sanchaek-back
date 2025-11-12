package glue.Gachi_Sanchaek.walkRecommendation.controller;

import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.walkRecommendation.dto.SaveWalkRouteResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRecommendationGroupResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRouteSelectionRequest;
import glue.Gachi_Sanchaek.walkRecommendation.service.WalkRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static glue.Gachi_Sanchaek.util.ApiResponse.ok;

@Tag(name = "산책 경로 추천 및 저장", description = "사용자의 위치 및 설정에 기반한 산책 경로 추천 및 저장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes")
public class WalkRecommendationController {
    private final WalkRecommendationService walkRecommendationService;


    @Operation(summary = "산책 경로 추천", description = "사용자의 현재 위치나, 기관위치를 바탕으로 원하는 산책 시간에 맞춰 추천 경로 목록(3개)을 제공합니다.")
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<WalkRecommendationGroupResponse>> getRecommend(
            @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails,

            @Parameter(description = "선택된 기관 ID (선택 사항)", required = false)
            @RequestParam(required = false) Long orgId,

            @Parameter(description = "원하는 산책 시간 (분 단위)")
            @RequestParam int minutes,

            @Parameter(description = "사용자의 현재 위도")
            @RequestParam double currentLat,

            @Parameter(description = "사용자의 현재 경도")
            @RequestParam double currentLng){
        WalkRecommendationGroupResponse recommendedRoutes = walkRecommendationService.recommendRoutes(orgId, minutes,currentLat,currentLng);
        return ok(recommendedRoutes);
    }

    @Operation(summary = "추천 경로 선택 및 저장", description = "사용자가 추천된 경로 중 하나를 선택하고, 해당 경로를 저장합니다.")
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<SaveWalkRouteResponse>> saveRoute(
            @AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody @Parameter(description = "선택된 산책 경로 정보")
            WalkRouteSelectionRequest req){

        if (userDetails == null || userDetails.getUserId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 유효하지 않거나 만료되었습니다.");
        }
        Long userId = userDetails.getUserId();
        SaveWalkRouteResponse saved = walkRecommendationService.saveSelectedRoute(userId,req);
        return ok(saved);
    }



}
