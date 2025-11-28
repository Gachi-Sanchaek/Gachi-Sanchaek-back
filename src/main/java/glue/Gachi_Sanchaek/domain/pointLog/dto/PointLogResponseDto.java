package glue.Gachi_Sanchaek.domain.pointLog.dto;

import glue.Gachi_Sanchaek.domain.pointLog.entity.PointLog;
import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointLogResponseDto {
    private Long amount;
    private LocalDateTime date;
    private WalkType type;
    private String location;

    public PointLogResponseDto(PointLog pointLog) {
        this.amount = pointLog.getAmount();
        this.date = pointLog.getCreatedAt();
        this.type = pointLog.getType();
        this.location = pointLog.getLocation();
    }
}
