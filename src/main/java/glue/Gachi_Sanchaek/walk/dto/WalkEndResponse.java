package glue.Gachi_Sanchaek.walk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkEndResponse {
    private Long walkId;
    private Double totalDistance;
    private Long totalMin;
    private Long pointsEarned;
    private String message;
}
