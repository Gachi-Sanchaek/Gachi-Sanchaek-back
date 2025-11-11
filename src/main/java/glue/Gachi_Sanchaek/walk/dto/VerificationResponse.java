package glue.Gachi_Sanchaek.walk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationResponse {
    private Long walkId;
    private boolean verified;
    private String message;
}
