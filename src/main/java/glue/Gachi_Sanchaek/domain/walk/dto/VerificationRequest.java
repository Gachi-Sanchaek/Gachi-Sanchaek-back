package glue.Gachi_Sanchaek.domain.walk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class VerificationRequest {
    private Long walkId;
    private String qrToken;
}
