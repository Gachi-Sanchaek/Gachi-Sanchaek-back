package glue.Gachi_Sanchaek.organization.controller;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.service.KakaoMapService;
import glue.Gachi_Sanchaek.organization.service.OrganizationService;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final KakaoMapService kakaoMapService;
    private final OrganizationService organizationService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<OrganizationDTO>>> searchOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int radius,
            @RequestParam String keyword
    ){
        List<OrganizationDTO> result = kakaoMapService.searchNearbyOrganizations(lat,lng,radius,keyword);
        return ApiResponse.ok(result);
    }

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<OrganizationResponse>> selectOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestBody OrganizationDTO selectedOrg){

        OrganizationResponse saved = organizationService.saveSelectedOrganization(userDetails.getUserId(), keyword,selectedOrg);

        return ApiResponse.ok(saved);
    }
}
