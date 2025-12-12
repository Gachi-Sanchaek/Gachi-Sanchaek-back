package glue.Gachi_Sanchaek.domain.walk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalkEndRequest {
    private Long walkId;
    private Double totalDistance;
    private Integer totalSeconds;
}
