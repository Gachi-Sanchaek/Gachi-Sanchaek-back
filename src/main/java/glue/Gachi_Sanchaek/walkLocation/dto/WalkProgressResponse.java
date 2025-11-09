package glue.Gachi_Sanchaek.walkLocation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkProgressResponse {
    private int status;
    private String message;
    private Long walkId;
    private Double distanceKm;
    private Long totalMin;
    private Double currentLat;
    private Double currentLng;
}
