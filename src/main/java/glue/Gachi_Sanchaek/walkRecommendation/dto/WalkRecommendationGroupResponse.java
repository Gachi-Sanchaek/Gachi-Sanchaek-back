package glue.Gachi_Sanchaek.walkRecommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "산책 경로 추천 그룹 응답 객체 (추천 경로 목록을 포함)")
public class WalkRecommendationGroupResponse {
    @Schema(description = "현재 추천 세트의 고유 식별자")
    private String recommendationGroupId;

    @Schema(description = "경로 추천 시 기준이 된 기관의 ID (기관 기반 추천이 아닐 경우 null)")
    private Long orgId;

    @Schema(description = "추천된 개별 산책 경로 목록 (WalkRouteResponse 객체 리스트)")
    private List<WalkRouteResponse> routes;
}
