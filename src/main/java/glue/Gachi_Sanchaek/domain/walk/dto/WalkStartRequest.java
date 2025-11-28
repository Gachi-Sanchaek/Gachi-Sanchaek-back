package glue.Gachi_Sanchaek.domain.walk.dto;

import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WalkStartRequest {
    private Long recommendationId;
    private WalkType walkType;
}
