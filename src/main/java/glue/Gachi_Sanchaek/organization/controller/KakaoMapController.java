package glue.Gachi_Sanchaek.organization.controller;

import glue.Gachi_Sanchaek.common.ApiResponse;
import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;

import glue.Gachi_Sanchaek.organization.service.KakaoMapService;

import glue.Gachi_Sanchaek.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KakaoMapController {

    private final KakaoMapService kakaoMapService;

    @GetMapping("/organization/nearby")
    public ResponseEntity<ApiResponse<List<OrganizationDTO>>> searchOrganization(
            @AuthenticationPrincipal User user,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int radius,
            @RequestParam String keyword
    ){
        List<OrganizationDTO> result = kakaoMapService.searchNearbyOrganizations(lat,lng,radius,keyword);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
