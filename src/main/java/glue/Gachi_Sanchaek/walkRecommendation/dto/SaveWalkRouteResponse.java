package glue.Gachi_Sanchaek.walkRecommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "산책 경로 저장 결과 응답 객체")
public class SaveWalkRouteResponse {

    @Schema(description = "저장된 산책 경로의 고유 ID")
    private Long walkRecommendationId;

    @Schema(description = "저장 작업에 대한 결과 메시지")
    private String message;

    @Schema(description = "경로가 저장된 시간")
    private LocalDateTime savedAt;
}