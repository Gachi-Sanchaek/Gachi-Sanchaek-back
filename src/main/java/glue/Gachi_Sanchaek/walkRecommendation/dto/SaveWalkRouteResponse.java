package glue.Gachi_Sanchaek.walkRecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveWalkRouteResponse {
    private Long walkRecommendationId;
    private String message;
    private LocalDateTime savedAt;
}