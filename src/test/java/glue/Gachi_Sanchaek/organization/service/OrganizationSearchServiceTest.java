package glue.Gachi_Sanchaek.organization.service;

import glue.Gachi_Sanchaek.domain.organization.dto.OrganizationDTO;
import glue.Gachi_Sanchaek.domain.organization.service.KakaoMapService;
import glue.Gachi_Sanchaek.domain.organization.service.OrganizationSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrganizationSearchServiceTest {

    @Mock
    KakaoMapService kakaoMapService;

    @InjectMocks
    OrganizationSearchService organizationSearchService;

    @Test
    @DisplayName("SENIOR 타입으로 검색 시, 노인복지 관련 기관만 필터링해서 10개 이하로 반환한다")
    void searchNearby_senior(){
        // Given
        double lat = 37.5665;
        double lng = 126.9780;
        int radius = 1000;

        // 노인복지관 키워드로 나온 결과
        OrganizationDTO senior1 = OrganizationDTO.builder()
                .kakaoId(1L)
                .name("행복노인복지관")
                .address("서울 어딘가")
                .latitude(lat)
                .longitude(lng)
                .build();

        //복지관은 있지만 제외키워드(요양병원) 포함
        OrganizationDTO notSenior = OrganizationDTO.builder()
                .kakaoId(2L)
                .name("행복요양병원 노인복지관")
                .address("서울 어딘가2")
                .latitude(lat)
                .longitude(lng)
                .build();
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인복지관"))
                .thenReturn(List.of(senior1, notSenior));

        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인종합복지관"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인센터"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "종합사회복지관"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인문화센터"))
                .thenReturn(List.of());

        //when
        List<OrganizationDTO> result =
                organizationSearchService.searchNearby(lat,lng,radius, OrganizationSearchService.SearchType.SENIOR);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("행복노인복지관");
    }

    @Test
    @DisplayName("ANIMAL 타입으로 검색 시, 동물보호 관련 기관만 필터링해서 10개 이하로 반환한다")
    void searchNearby_animal() {
        // Given
        double lat = 37.5665;
        double lng = 126.9780;
        int radius = 1000;

        OrganizationDTO shelter = OrganizationDTO.builder()
                .kakaoId(10L)
                .name("서울동물보호센터")
                .address("서울 어딘가")
                .latitude(lat)
                .longitude(lng)
                .build();

        // 제외 키워드에 걸려서 걸러져야 하는 값
        OrganizationDTO animalHospital = OrganizationDTO.builder()
                .kakaoId(11L)
                .name("행복동물병원")
                .address("서울 어딘가2")
                .latitude(lat)
                .longitude(lng)
                .build();

        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "동물보호"))
                .thenReturn(List.of(shelter, animalHospital));
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "동물보호센터"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "보호소"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "보호센터"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "입양센터"))
                .thenReturn(List.of());

        // When
        List<OrganizationDTO> result =
                organizationSearchService.searchNearby(lat, lng, radius, OrganizationSearchService.SearchType.ANIMAL);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("서울동물보호센터");
    }
    @Test
    @DisplayName("중복 kakaoId 는 하나로 합쳐지고 최대 10개만 반환한다")
    void searchNearby_deduplicate_and_limit() {
        // Given
        double lat = 37.5665;
        double lng = 126.9780;
        int radius = 1000;

        // kakaoId 1L 이 중복으로 들어와도 하나만 남아야 함
        OrganizationDTO dto1 = OrganizationDTO.builder()
                .kakaoId(1L)
                .name("행복노인복지관")
                .build();
        OrganizationDTO dto1Dup = OrganizationDTO.builder()
                .kakaoId(1L)
                .name("행복노인복지관 지점") // 이름은 달라도 kakaoId 같으므로 머지
                .build();

        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인복지관"))
                .thenReturn(List.of(dto1, dto1Dup));

        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인종합복지관"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인센터"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "종합사회복지관"))
                .thenReturn(List.of());
        when(kakaoMapService.searchNearbyOrganizations(lat, lng, radius, "노인문화센터"))
                .thenReturn(List.of());

        // When
        List<OrganizationDTO> result =
                organizationSearchService.searchNearby(lat, lng, radius, OrganizationSearchService.SearchType.SENIOR);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKakaoId()).isEqualTo(1L);
    }
}
