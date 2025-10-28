package glue.Gachi_Sanchaek.walkRecommendation.dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalkRouteSelectionRequest {
    private Long orgId;
    private String groupId;
    private WalkRouteResponse selectedRoute;
}
