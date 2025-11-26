package glue.Gachi_Sanchaek.domain.organization.dto;

import glue.Gachi_Sanchaek.domain.organization.entity.Organization;
import glue.Gachi_Sanchaek.domain.organization.entity.OrganizationCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "저장된 기관의 상세 정보 응답 객체")
public class OrganizationResponse {

    @Schema(description = "DB에 저장된 기관의 고유 ID")
    private Long id;

    @Schema(description = "카카오맵에서 제공하는 기관의 고유 ID")
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

    @Schema(description = "기관 분류(SHELTER,WELFARE)")
    private OrganizationCategory category;

    @Schema(description = "기관 정보가 DB에 저장된 시간")
    private LocalDateTime createdAt;
}
