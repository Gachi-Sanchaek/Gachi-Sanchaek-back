package glue.Gachi_Sanchaek.domain.organization.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "기관 정보 전송 객체")
public class OrganizationDTO {

    @Schema(description = "카카오맵에서 제공하는 기관의 고유 ID (내부 저장용)")
    private Long kakaoId; //저장용

    @Schema(description = "기관 이름")
    private String name;

    @Schema(description = "기관 전화번호")
    private String phone;

    @Schema(description = "기관 주소")
    private String address;

    @Schema(description = "기관 위치의 위도")
    private double latitude;

    @Schema(description = "기관 위치의 경도")
    private double longitude;

    @Schema(description = "사용자 위치로부터의 거리 (미터 단위)")
    private int distance;


}
