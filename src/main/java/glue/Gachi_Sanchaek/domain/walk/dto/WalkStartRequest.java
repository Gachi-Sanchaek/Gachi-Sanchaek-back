package glue.Gachi_Sanchaek.domain.walk.dto;

import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WalkStartRequest {
    private Long recommendationId;
    private WalkType walkType;
}
