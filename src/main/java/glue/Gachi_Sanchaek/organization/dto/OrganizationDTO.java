package glue.Gachi_Sanchaek.organization.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    private Long kakaoId; //저장용
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int distance;


}
