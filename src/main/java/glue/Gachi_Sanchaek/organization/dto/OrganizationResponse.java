package glue.Gachi_Sanchaek.organization.dto;

import glue.Gachi_Sanchaek.organization.entity.Organization;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrganizationResponse {
    private Long id;
    private Long kakaoId; //저장용
    private String name;
    private String phone;
    private String address;
    private double latitude;
    private double longitude;
    private Organization.OrganizationCategory category;
    private LocalDateTime createdAt;
}
