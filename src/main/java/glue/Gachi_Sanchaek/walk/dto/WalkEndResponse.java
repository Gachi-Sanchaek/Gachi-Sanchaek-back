package glue.Gachi_Sanchaek.walk.dto;

import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkEndResponse {
    private Long walkId;
    private WalkStatus status;
    private String nickname;
    private Double totalDistance;
    private String totalTime;
    private Long pointsEarned;
    private Long walkingCount;
    private String message;
}
