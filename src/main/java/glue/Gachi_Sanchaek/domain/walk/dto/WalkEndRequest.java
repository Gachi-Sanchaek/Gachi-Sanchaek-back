package glue.Gachi_Sanchaek.domain.walk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalkEndRequest {
    private Long walkId;
    private Double totalDistance;
    private Integer totalSeconds;
}
