package glue.Gachi_Sanchaek.pointLog.dto;

import glue.Gachi_Sanchaek.pointLog.entity.PointLog;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointLogResponseDto {
    private Long amount;
    private LocalDateTime date;
    private String title;
    private String location;

    public PointLogResponseDto(PointLog pointLog) {
        this.amount = pointLog.getAmount();
        this.date = pointLog.getCreatedAt();
        this.title = pointLog.getTitle();
        this.location = pointLog.getLocation();
    }
}
