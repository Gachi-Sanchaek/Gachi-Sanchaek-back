package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.entity.OrganizationCategory;
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

        OrganizationCategory requiredCategory = mapKeywordToCategory(keyword);

        Organization org = organizationRepository.findByKakaoPlaceId(selectedOrg.getKakaoId())
                .orElseGet(() -> saveNewOrganization(selectedOrg, requiredCategory));

        saveUserOrganizationIfNotExists(userId, org);

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

    private Organization saveNewOrganization(OrganizationDTO dto, OrganizationCategory category) {
        return organizationRepository.save(
                Organization.builder()
                        .kakaoPlaceId(dto.getKakaoId())
                        .name(dto.getName())
                        .phone(dto.getPhone())
                        .address(dto.getAddress())
                        .latitude(dto.getLatitude())
                        .longitude(dto.getLongitude())
                        .category(category)
                        .createdAt(LocalDateTime.now())
                        .build()

        );
    }

    //  유저와 기관의 연결 정보가 없으면 새로 저장합니다.
    private void saveUserOrganizationIfNotExists(Long userId, Organization org) {
        if (!userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, org.getId())) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저 정보 ID " + userId + " 에 해당하는 정보를 찾을 수 없습니다"));

            userOrganizationRepository.save(
                    UserOrganization.builder()
                            .user(user)
                            .organization(org)
                            .build()
            );
        }
    }


    private OrganizationCategory mapKeywordToCategory(String keyword) {
        if (keyword.contains("복지")) return OrganizationCategory.WELFARE;
        if (keyword.contains("보호")) return OrganizationCategory.SHELTER;
        throw new IllegalArgumentException("알 수 없는 카테고리: " + keyword);
    }
}



