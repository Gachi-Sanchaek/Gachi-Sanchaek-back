package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    @Transactional
    public OrganizationResponse saveSelectedOrganization(String keyword, OrganizationDTO selectedOrg) {

        Organization.OrganizationCategory category = mapKeywordToCategory(keyword);

        Optional<Organization> exist = organizationRepository.findByKakaoPlaceId(selectedOrg.getKakaoId());

        if (exist.isPresent()) {
            Organization org = exist.get();
            return OrganizationResponse.builder()
                    .id(org.getId())
                    .kakaoId(org.getKakaoPlaceId())
                    .name(org.getName())
                    .address(org.getAddress())
                    .latitude(org.getLatitude())
                    .longitude(org.getLongitude())
                    .category(org.getCategory())
                    .createdAt(org.getCreatedAt())
                    .build();
        }

        Organization newOrg = Organization.builder()
                .kakaoPlaceId(selectedOrg.getKakaoId())
                .name(selectedOrg.getName())
                .address(selectedOrg.getAddress())
                .latitude(selectedOrg.getLatitude())
                .longitude(selectedOrg.getLongitude())
                .category(category)
                .createdAt(LocalDateTime.now())
                .build();

        Organization saved = organizationRepository.save(newOrg);

        OrganizationResponse res = OrganizationResponse.builder()
                .id(saved.getId())
                .kakaoId(saved.getKakaoPlaceId())
                .name(saved.getName())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .category(saved.getCategory())
                .createdAt(saved.getCreatedAt())
                .build();

        return res;

    }

    private Organization.OrganizationCategory mapKeywordToCategory(String keyword) {
        if (keyword.contains("복지")) return Organization.OrganizationCategory.WELFARE;
        if (keyword.contains("보호")) return Organization.OrganizationCategory.SHELTER;
        throw new IllegalArgumentException("알 수 없는 카테고리: " + keyword);
    }

}
