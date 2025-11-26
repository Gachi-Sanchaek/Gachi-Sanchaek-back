package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.domain.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.domain.organization.dto.OrganizationResponse;
import glue.Gachi_Sanchaek.domain.organization.entity.Organization;
import glue.Gachi_Sanchaek.domain.organization.entity.OrganizationCategory;
import glue.Gachi_Sanchaek.domain.organization.entity.UserOrganization;
import glue.Gachi_Sanchaek.domain.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.domain.organization.repository.UserOrganizationRepository;
import glue.Gachi_Sanchaek.domain.organization.service.OrganizationService;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static glue.Gachi_Sanchaek.domain.organization.service.OrganizationSearchService.SearchType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserOrganizationRepository userOrganizationRepository;

    @InjectMocks
    OrganizationService organizationService;

    @Test
    @DisplayName("신규 기관 선택 시, Organization 과 UserOrganization 이 저장되고 응답 DTO 를 반환한다")
    void saveSelectedOrganization_newOrganization_saves_org_and_userOrg() {
        // Given
        Long userId = 1L;
        Long kakaoId = 100L;

        OrganizationDTO selectedOrg = OrganizationDTO.builder()
                .kakaoId(kakaoId)
                .name("행복노인복지관")
                .phone("010-0000-0000")
                .address("서울 어딘가")
                .latitude(37.0)
                .longitude(127.0)
                .build();


        when(organizationRepository.findByKakaoPlaceId(kakaoId))
                .thenReturn(Optional.empty());


        Organization savedOrg = Organization.builder()
                .id(10L)
                .kakaoPlaceId(kakaoId)
                .name(selectedOrg.getName())
                .phone(selectedOrg.getPhone())
                .address(selectedOrg.getAddress())
                .latitude(selectedOrg.getLatitude())
                .longitude(selectedOrg.getLongitude())
                .category(OrganizationCategory.WELFARE) // SENIOR → WELFARE
                .qrCodePayload(String.valueOf(kakaoId))
                .createdAt(LocalDateTime.now())
                .build();

        when(organizationRepository.save(any(Organization.class)))
                .thenReturn(savedOrg);

        // UserOrganization 에 아직 매핑이 없다고 가정
        when(userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, 10L))
                .thenReturn(false);

        // 유저 조회
        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // When
        OrganizationResponse response =
                organizationService.saveSelectedOrganization(userId, SearchType.SENIOR, selectedOrg);

        // Then
        // 응답 DTO 검증
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getKakaoId()).isEqualTo(kakaoId);
        assertThat(response.getName()).isEqualTo("행복노인복지관");
        assertThat(response.getCategory()).isEqualTo(OrganizationCategory.WELFARE);

        // Organization save 가 한 번 호출되었는지 확인
        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(orgCaptor.capture());
        Organization orgToSave = orgCaptor.getValue();
        assertThat(orgToSave.getKakaoPlaceId()).isEqualTo(kakaoId);
        assertThat(orgToSave.getName()).isEqualTo(selectedOrg.getName());

        // UserOrganization 저장 확인
        ArgumentCaptor<UserOrganization> userOrgCaptor = ArgumentCaptor.forClass(UserOrganization.class);
        verify(userOrganizationRepository).save(userOrgCaptor.capture());
        UserOrganization savedUserOrg = userOrgCaptor.getValue();
        assertThat(savedUserOrg.getUser().getId()).isEqualTo(userId);
        assertThat(savedUserOrg.getOrganization().getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("이미 존재하는 기관이 있고 유저-기관 매핑도 존재하면, 새로 저장하지 않는다")
    void saveSelectedOrganization_existingOrg_and_existingUserOrg() {
        // Given
        Long userId = 1L;
        Long kakaoId = 200L;

        OrganizationDTO selectedOrg = OrganizationDTO.builder()
                .kakaoId(kakaoId)
                .name("기존노인복지관")
                .build();

        Organization existingOrg = Organization.builder()
                .id(20L)
                .kakaoPlaceId(kakaoId)
                .name("기존노인복지관")
                .category(OrganizationCategory.WELFARE)
                .qrCodePayload("200") // 이미 qrCodePayload 도 있음
                .createdAt(LocalDateTime.now())
                .build();

        when(organizationRepository.findByKakaoPlaceId(kakaoId))
                .thenReturn(Optional.of(existingOrg));

        // 이미 user-org 매핑이 존재
        when(userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, 20L))
                .thenReturn(true);

        // When
        OrganizationResponse response =
                organizationService.saveSelectedOrganization(userId, SearchType.SENIOR, selectedOrg);

        // Then
        // Organization 은 새로 save 되지 않아야 함
        verify(organizationRepository, never()).save(any());


        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getKakaoId()).isEqualTo(kakaoId);
        assertThat(response.getName()).isEqualTo("기존노인복지관");
        assertThat(response.getCategory()).isEqualTo(OrganizationCategory.WELFARE);
    }

    @Test
    @DisplayName("getLocationName 은 유저에게 매핑된 첫 기관 이름을 반환한다")
    void getLocationName_exists() {
        // Given
        Long userId = 1L;

        Organization org = Organization.builder()
                .id(10L)
                .name("행복노인복지관")
                .build();

        User user = User.builder()
                .id(userId)
                .build();

        UserOrganization userOrg = UserOrganization.builder()
                .id(100L)
                .user(user)
                .organization(org)
                .build();

        when(userOrganizationRepository.findFirstByUser_Id(userId))
                .thenReturn(Optional.of(userOrg));

        // When
        String locationName = organizationService.getLocationName(userId);

        // Then
        assertThat(locationName).isEqualTo("행복노인복지관");
    }

    @Test
    @DisplayName("getLocationName 은 매핑된 기관이 없으면 빈 문자열을 반환한다")
    void getLocationName_notExists() {
        // Given
        Long userId = 1L;
        when(userOrganizationRepository.findFirstByUser_Id(userId))
                .thenReturn(Optional.empty());

        // When
        String locationName = organizationService.getLocationName(userId);

        // Then
        assertThat(locationName).isEqualTo("");
    }
}
