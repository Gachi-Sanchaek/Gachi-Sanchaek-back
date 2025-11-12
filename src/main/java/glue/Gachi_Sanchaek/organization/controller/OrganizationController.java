package glue.Gachi_Sanchaek.organization.controller;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.service.KakaoMapService;

import glue.Gachi_Sanchaek.organization.service.OrganizationSearchService;
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

    private final OrganizationSearchService organizationSearchService;
    private final OrganizationService organizationService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<OrganizationDTO>>> searchNearby(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int radius,
            @RequestParam OrganizationSearchService.SearchType type
            ){
        List<OrganizationDTO> result = organizationSearchService.searchNearby(lat,lng,radius,type);
    
        return ApiResponse.ok(result);
    }

    @PostMapping("/select")
    public ResponseEntity<ApiResponse<OrganizationResponse>> selectOrganization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam OrganizationSearchService.SearchType type,
            @RequestBody OrganizationDTO selectedOrg){

        OrganizationResponse saved = organizationService.saveSelectedOrganization(userDetails.getUserId(), type,selectedOrg);

        return ApiResponse.ok(saved);
    }
}
