package glue.Gachi_Sanchaek.domain.walkRecommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "산책 경로 선택 및 저장 요청 객체")
public class WalkRouteSelectionRequest {

    @Schema(description = "선택된 경로가 연결된 기관의 ID(기관이 없을 경우 null)")
    private Long orgId;

    @Schema(description = "경로를 추천받았던 그룹의 고유 식별자")
    private String groupId;

    @Schema(description = "사용자가 최종적으로 선택한 경로의 상세 정보 (WalkRouteResponse 객체)")
    private WalkRouteResponse selectedRoute;
}
