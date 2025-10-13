package glue.Gachi_Sanchaek.walk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class WalkStartRequest {
    private Long recommendationId;
    private String walkType;
    private Integer desiredTime; //분 단위
}
