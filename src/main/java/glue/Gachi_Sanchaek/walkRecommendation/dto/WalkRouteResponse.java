package glue.Gachi_Sanchaek.walkRecommendation.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalkRouteResponse {
    private Long id;
    private String description;
    private int estimatedTime;
    private List<Waypoint> waypoints;

}
