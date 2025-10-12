package glue.Gachi_Sanchaek.walkRecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Waypoint {
    private double lat;
    private double lng;
}
