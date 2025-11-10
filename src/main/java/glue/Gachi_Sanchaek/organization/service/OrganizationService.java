package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.entity.UserOrganization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.organization.repository.UserOrganizationRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    @Transactional
    public OrganizationResponse saveSelectedOrganization(
            Long userId, String keyword, OrganizationDTO selectedOrg) {

        Organization.OrganizationCategory category = mapKeywordToCategory(keyword);

        Organization org = organizationRepository.findByKakaoPlaceId(selectedOrg.getKakaoId())
                .orElseGet(() -> organizationRepository.save(
                        Organization.builder()
                                .kakaoPlaceId(selectedOrg.getKakaoId())
                                .name(selectedOrg.getName())
                                .phone(selectedOrg.getPhone())
                                .address(selectedOrg.getAddress())
                                .latitude(selectedOrg.getLatitude())
                                .longitude(selectedOrg.getLongitude())
                                .category(category)
                                .qrCodePayload(String.valueOf(selectedOrg.getKakaoId()))
                                .createdAt(LocalDateTime.now())
                                .build()
                ));

        if (org.getQrCodePayload() == null) {
            org.setQrCodePayload(String.valueOf(org.getKakaoPlaceId()));
        }

        //유저-기관 에 저장이 안되어있음!
        if(!userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, org.getId())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다"));

            userOrganizationRepository.save(
                    UserOrganization.builder()
                            .user(user)
                            .organization(org)
                            .build()
            );
        }
        return OrganizationResponse.builder()
                .id(org.getId())
                .kakaoId(org.getKakaoPlaceId())
                .name(org.getName())
                .phone(org.getPhone())
                .address(org.getAddress())
                .latitude(org.getLatitude())
                .longitude(org.getLongitude())
                .category(org.getCategory())
                .createdAt(org.getCreatedAt())
                .build();

    }

    private Organization.OrganizationCategory mapKeywordToCategory(String keyword) {
        if (keyword.contains("복지")) return Organization.OrganizationCategory.WELFARE;
        if (keyword.contains("보호")) return Organization.OrganizationCategory.SHELTER;
        throw new IllegalArgumentException("알 수 없는 카테고리: " + keyword);
    }

}
