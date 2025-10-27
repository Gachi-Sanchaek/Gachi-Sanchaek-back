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
public class WalkRouteResponse {
    private Long id;
    private String description;
    private List<Waypoint> waypoints;
    private int estimatedTime;

}
