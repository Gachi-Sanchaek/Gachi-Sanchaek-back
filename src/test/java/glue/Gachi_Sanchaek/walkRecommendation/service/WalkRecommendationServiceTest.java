package glue.Gachi_Sanchaek.walkRecommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.domain.organization.entity.Organization;
import glue.Gachi_Sanchaek.domain.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.domain.organization.repository.UserOrganizationRepository;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.SaveWalkRouteResponse;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.WalkRecommendationGroupResponse;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.WalkRouteResponse;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.WalkRouteSelectionRequest;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.Waypoint;
import glue.Gachi_Sanchaek.domain.walkRecommendation.service.GeminiService;
import glue.Gachi_Sanchaek.domain.walkRecommendation.service.WalkRecommendationService;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.repository.UserRepository;
import glue.Gachi_Sanchaek.domain.walkRecommendation.entity.WalkRecommendation;
import glue.Gachi_Sanchaek.domain.walkRecommendation.repository.WalkRecommendationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalkRecommendationServiceTest {

    @Mock
    OrganizationRepository organizationRepository;

    @Mock
    UserOrganizationRepository userOrganizationRepository;

    @Mock
    GeminiService geminiService;

    @Spy // ObjectMapper는 실제 인스턴스를 사용 (Mockito가 기본 생성자로 만들어줌)
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    UserRepository userRepository;

    @Mock
    WalkRecommendationRepository walkRecommendationRepository;

    @InjectMocks
    WalkRecommendationService walkRecommendationService;


    @Test
    @DisplayName("기관 ID가 주어지면 해당 기관의 위치를 기준으로 산책 코스를 추천받아 파싱한다")
    void recommendRoutes_withOrg() {
        // Given
        Long orgId = 1L;
        int minutes = 30;
        double currentLat = 0.0;
        double currentLng = 0.0;

        Organization org = Organization.builder()
                .id(orgId)
                .name("행복복지관")
                .latitude(37.1234)
                .longitude(127.5678)
                .build();

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(org));

        // Gemini에서 내려주는 JSON 응답 예시
        String geminiJson = """
                {
                  "routes": [
                    {
                      "id": 1,
                      "description": "잔잔한 공원 산책",
                      "waypoints": [
                        {"lat": 37.1234, "lng": 127.5678},
                        {"lat": 37.1235, "lng": 127.5680}
                      ],
                      "estimatedTime": 30
                    }
                  ]
                }
                """;

        when(geminiService.generateRoutesFromGemini(any(String.class)))
                .thenReturn(geminiJson);

        // When
        WalkRecommendationGroupResponse response =
                walkRecommendationService.recommendRoutes(orgId, minutes, currentLat, currentLng);

        // Then
        assertThat(response.getOrgId()).isEqualTo(orgId);
        assertThat(response.getRoutes()).hasSize(1);

        WalkRouteResponse route = response.getRoutes().get(0);
        assertThat(route.getId()).isEqualTo(1L);
        assertThat(route.getDescription()).isEqualTo("잔잔한 공원 산책");
        assertThat(route.getEstimatedTime()).isEqualTo(30);
        assertThat(route.getWaypoints()).hasSize(2);

        // Gemini 호출 시 프롬프트 안에 기관 이름이 포함됐는지도 확인 가능
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(geminiService).generateRoutesFromGemini(promptCaptor.capture());
        String usedPrompt = promptCaptor.getValue();
        assertThat(usedPrompt).contains("행복복지관");
    }

    @Test
    @DisplayName("기관 ID가 없으면 현재 위치를 기준으로 산책 코스를 추천받아 파싱한다")
    void recommendRoutes_withoutOrg() {
        // Given
        Long orgId = null;
        int minutes = 20;
        double currentLat = 37.5;
        double currentLng = 127.0;

        String geminiJson = """
                {
                  "routes": [
                    {
                      "id": 10,
                      "description": "현재 위치 근처 한 바퀴",
                      "waypoints": [
                        {"lat": 37.5, "lng": 127.0},
                        {"lat": 37.5005, "lng": 127.0005}
                      ],
                      "estimatedTime": 20
                    }
                  ]
                }
                """;

        when(geminiService.generateRoutesFromGemini(any(String.class)))
                .thenReturn(geminiJson);

        // When
        WalkRecommendationGroupResponse response =
                walkRecommendationService.recommendRoutes(orgId, minutes, currentLat, currentLng);

        // Then
        assertThat(response.getOrgId()).isNull();   // 기관 없이 현재위치 기준
        assertThat(response.getRoutes()).hasSize(1);
        assertThat(response.getRoutes().get(0).getId()).isEqualTo(10L);
    }



    @Test
    @DisplayName("선택한 경로와 기관이 유효하면 산책 경로를 저장하고 응답 DTO를 반환한다")
    void saveSelectedRoute_success_withOrg() {
        // Given
        Long userId = 1L;
        Long orgId = 100L;

        User user = User.builder()
                .id(userId)
                .build();

        Organization org = Organization.builder()
                .id(orgId)
                .name("행복복지관")
                .build();

        WalkRouteResponse selectedRoute = WalkRouteResponse.builder()
                .id(1L)
                .description("잔잔한 코스")
                .estimatedTime(30)
                .waypoints(List.of(
                        new Waypoint(37.1, 127.1),
                        new Waypoint(37.2, 127.2)
                ))
                .build();

        WalkRouteSelectionRequest req = WalkRouteSelectionRequest.builder()
                .groupId(UUID.randomUUID().toString())
                .orgId(orgId)
                .selectedRoute(selectedRoute)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(org));

        // 유저가 이 기관을 이미 저장한 상태라고 가정
        when(userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, orgId))
                .thenReturn(true);

        WalkRecommendation saved = WalkRecommendation.builder()
                .id(999L)
                .user(user)
                .organization(org)
                .groupId(req.getGroupId())
                .description(selectedRoute.getDescription())
                .plannedMinutes(selectedRoute.getEstimatedTime())
                .wayPoints(selectedRoute.getWaypoints())
                .createdAt(LocalDateTime.now())
                .build();

        when(walkRecommendationRepository.save(any(WalkRecommendation.class)))
                .thenReturn(saved);

        // When
        SaveWalkRouteResponse response =
                walkRecommendationService.saveSelectedRoute(userId, req);

        // Then
        assertThat(response.getWalkRecommendationId()).isEqualTo(999L);
        assertThat(response.getMessage()).isEqualTo("산책경로가 성공적으로 저장되었습니다");

        ArgumentCaptor<WalkRecommendation> captor = ArgumentCaptor.forClass(WalkRecommendation.class);
        verify(walkRecommendationRepository).save(captor.capture());
        WalkRecommendation entityToSave = captor.getValue();

        assertThat(entityToSave.getUser().getId()).isEqualTo(userId);
        assertThat(entityToSave.getOrganization().getId()).isEqualTo(orgId);
        assertThat(entityToSave.getWayPoints()).hasSize(2);
    }

    @Test
    @DisplayName("선택한 경로에 waypoint가 없으면 예외를 던진다")
    void saveSelectedRoute_noWaypoints_throws() {
        // Given
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        WalkRouteResponse selectedRoute = WalkRouteResponse.builder()
                .id(1L)
                .description("비어있는 코스")
                .estimatedTime(30)
                .waypoints(List.of())  // waypoints 비어있음
                .build();

        WalkRouteSelectionRequest req = WalkRouteSelectionRequest.builder()
                .groupId("group-1")
                .orgId(null)   // 기관 없이 저장
                .selectedRoute(selectedRoute)
                .build();

        // When & Then
        assertThatThrownBy(() -> walkRecommendationService.saveSelectedRoute(userId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("선택 경로가 비어있습니다");
    }

    @Test
    @DisplayName("기관이 존재하지만 유저와 매핑되어 있지 않으면 예외를 던진다")
    void saveSelectedRoute_orgNotLinked_throws() {
        // Given
        Long userId = 1L;
        Long orgId = 200L;

        User user = User.builder()
                .id(userId)
                .build();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Organization org = Organization.builder()
                .id(orgId)
                .name("연결 안 된 기관")
                .build();
        when(organizationRepository.findById(orgId))
                .thenReturn(Optional.of(org));

        // 유저-기관 링크 없음
        when(userOrganizationRepository.existsByUser_IdAndOrganization_Id(userId, orgId))
                .thenReturn(false);

        WalkRouteResponse selectedRoute = WalkRouteResponse.builder()
                .id(1L)
                .description("테스트 코스")
                .estimatedTime(20)
                .waypoints(List.of(new Waypoint(37.1, 127.1)))
                .build();

        WalkRouteSelectionRequest req = WalkRouteSelectionRequest.builder()
                .groupId("group-2")
                .orgId(orgId)
                .selectedRoute(selectedRoute)
                .build();

        // When & Then
        assertThatThrownBy(() -> walkRecommendationService.saveSelectedRoute(userId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자가 저장한 기관이 아닙니다");
    }
}