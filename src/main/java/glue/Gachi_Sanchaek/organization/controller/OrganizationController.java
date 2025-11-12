package glue.Gachi_Sanchaek.organization.controller;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.service.KakaoMapService;

import glue.Gachi_Sanchaek.organization.service.OrganizationSearchService;
import glue.Gachi_Sanchaek.organization.service.OrganizationService;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name ="기관 관리 (Organization)", description ="주변 기관 검색 및 저장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationSearchService organizationSearchService;
    private final OrganizationService organizationService;

    @Operation(summary = "주변 기관 검색", description = "사용자 위치를 기반으로 특정 반경 내 기관을 검색합니다.")
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<OrganizationDTO>>> searchNearby(
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            CustomUserDetails userDetails,

            @Parameter(description = "사용자의 위도")
            @RequestParam double lat,

            @Parameter(description = "사용자의 경도")
            @RequestParam double lng,

            @Parameter(description = "검색 반경 (미터 단위)")
            @RequestParam int radius,

            @Parameter(description = "기관 type")
            @RequestParam OrganizationSearchService.SearchType type
            ){
        List<OrganizationDTO> result = organizationSearchService.searchNearby(lat,lng,radius,type);
    
        return ApiResponse.ok(result);
    }

    @Operation(summary = "기관 선택 및 저장", description = "사용자가 특정 기관을 선택하고, 해당 정보를 저장합니다.")
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<OrganizationResponse>> selectOrganization(
            @AuthenticationPrincipal @Parameter(hidden = true)CustomUserDetails userDetails,
            @Parameter(description = "검색에 사용된 타입 (type)")
            @RequestParam String keyword,
            @Parameter(description = "저장할 기관 정보")
            @RequestBody OrganizationDTO selectedOrg){

        OrganizationResponse saved = organizationService.saveSelectedOrganization(userDetails.getUserId(), keyword,selectedOrg);

        return ApiResponse.ok(saved);
    }
}
