package glue.Gachi_Sanchaek.domain.walk.dto;

import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.domain.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.domain.walk.enums.WalkStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class WalkResponse {
    private Long walkId;
    private WalkStatus status;
    private WalkType walkType;
    private Long recommendationId;
    private VerificationMethod verificationMethod;
    private LocalDateTime startTime;
}
