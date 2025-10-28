package glue.Gachi_Sanchaek.walkRecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalkRecommendationGroupResponse {
    private String recommendationGroupId;
    private List<WalkRouteResponse> routes;
}
