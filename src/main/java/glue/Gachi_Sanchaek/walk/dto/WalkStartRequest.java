package glue.Gachi_Sanchaek.walk.dto;

import glue.Gachi_Sanchaek.pointLog.enums.WalkType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class WalkStartRequest {
    private Long recommendationId;
    private WalkType walkType;
}
