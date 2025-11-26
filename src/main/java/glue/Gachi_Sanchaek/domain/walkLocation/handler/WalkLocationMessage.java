package glue.Gachi_Sanchaek.domain.walkLocation.handler;

import lombok.*;

@Getter
@Setter
public class WalkLocationMessage {
    private Long walkId;
    private Double lat;
    private Double lng;
}
