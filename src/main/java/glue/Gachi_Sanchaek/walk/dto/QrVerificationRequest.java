package glue.Gachi_Sanchaek.walk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class QrVerificationRequest {
    private Long walkId;
    private String qrToken;
    private Double totalDistance;
    private Integer totalMinutes;
}
