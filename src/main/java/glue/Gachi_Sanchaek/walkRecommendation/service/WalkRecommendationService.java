package glue.Gachi_Sanchaek.walkRecommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.organization.repository.UserOrganizationRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.walkRecommendation.dto.*;
import glue.Gachi_Sanchaek.walkRecommendation.entity.WalkRecommendation;
import glue.Gachi_Sanchaek.walkRecommendation.repository.WalkRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkRecommendationService {

    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final WalkRecommendationRepository walkRecommendationRepository;

    public WalkRecommendationGroupResponse recommendRoutes(Long orgId, int minutes, double currentLat,double currentLng) {

        Organization org = null;

        if(orgId != null) {
            org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new IllegalArgumentException("기관ID "+ orgId+"를 찾을 수 없습니다"));
        }

        String prompt = buildPrompt(org, currentLat,currentLng,minutes);
        String geminiResult = geminiService.generateRoutesFromGemini(prompt);

        // JSON 파싱해서 DTO 변환
        List<WalkRouteResponse> routes = parseRoutes(geminiResult);

        return WalkRecommendationGroupResponse.builder()
                .recommendationGroupId(UUID.randomUUID().toString())
                .orgId(org != null ? org.getId() : null)
                .routes(routes)
                .build();

    }

    private String buildPrompt(Organization org, double currentLat, double currentLng, int minutes) {
        String locationName;
        double lat;
        double lng;

        if(org != null) {
            locationName = org.getName();
            lat = org.getLatitude();
            lng = org.getLongitude();
        }else{
            locationName = "현재위치";
            lat = currentLat;
            lng = currentLng;
        }
        return String.format("""
                당신은 산책 코스를 추천하는 AI입니다.
                
                [요구사항]
                -출발지와 도착지는 모두 "%s"(위도: %f, 경도: %f) 입니다.
                -추천 경로는 간단하게 주변 산책을 할 수 있는 코스여야 합니다.
                -왕복 기준 %d분 동안 걸을 수 있는 경로를 3개 추천해주세요.
                -각 코스마다 15글자 이내의 설명을 넣어주세요.
                -코스의 설명을 추천코스의 특징을 잘 살리면서, 산책을 하고 싶다는 욕구를 불어일으키는 문구로 넣어주세요.
                -한 코스에서 같은 경로를 지날 수 없습니다.
                -각 코스는 서로 겹치지 않게 구성해주세요.
                -각 코스에는 id, description, estimatedTime, waypoint(lat,lng)를 포함한 JSON 형태로 응답해주세요.
                -waypoint에서 lat, lng 의 개수는 5개씩 주세요.
                
                [추가 조건]
                -건물 블록 한 개만 빙 둘러싸는 형태(정사각형/직사각형 루프)로 코스를 구성하지 마세요.
                -너무 집 주변만 맴도는 좁은 동선이 아니라, 출발지 반경 200~500m 사이의 넓은 지역을 활용해주세요.
                세 가지 산책 코스를 모두 생성하는 데 총 5초 이내로 완료될 수 있도록 경로 계산을 단순화해주세요.
                
                다음 형식의 순수 JSON만 반환하세요. 코드 블록, 백틱, 설명, 추가 텍스트 금지.
                
                [응답형식(JSON)]
                {
                          "routes": [
                            {
                              "id": <숫자>,
                              "description": "<설명>",
                              "waypoints": [{"lat": <위도>, "lng": <경도>}, ...],
                              "estimatedTime": <숫자>
                            },
                            ...
                          ]
                        }
                
                """, locationName, lat, lng, minutes);

    }

    private List<WalkRouteResponse> parseRoutes(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode routesNode = node.get("routes");
            List<WalkRouteResponse> routes = new ArrayList<>();

            if (routesNode == null || !routesNode.isArray()) {
                // "routes" 필드가 없거나 배열이 아니면 유효하지 않은 응답으로 처리
                log.error("Gemini 응답 JSON에 'routes' 배열이 없거나 형식이 잘못되었습니다: {}", json);
                throw new IllegalArgumentException("Gemini 응답 JSON에 'routes' 배열이 없습니다.");
            }

            for (JsonNode routeNode : routesNode) {
                if (!routeNode.has("id") || !routeNode.has("description") || !routeNode.has("waypoints") || !routeNode.has("estimatedTime")) {
                    log.warn("경로 항목에서 필수 필드가 누락되었습니다 {}" ,routeNode.toString());
                    continue;
                }

                List<Waypoint> waypoints = new ArrayList<>();
                JsonNode waypointsNode = routeNode.get("waypoints");
                if(waypointsNode.isArray()) {
                    for (JsonNode wp : waypointsNode) {
                        if (wp.has("lat")&& wp.has("lng")) {
                            waypoints.add(new Waypoint(wp.get("lat").asDouble(), wp.get("lng").asDouble()));
                        }
                    }
                }

                routes.add(WalkRouteResponse.builder()
                        .id(routeNode.get("id").asLong())
                        .description(routeNode.get("description").asText())
                        .waypoints(waypoints)
                        .estimatedTime(routeNode.get("estimatedTime").asInt())
                        .build());
            }
            return routes;
        } catch (IOException e) {
            log.error("Gemini 응답 JSON 파싱 실패: 원본 JSON: {}", json, e);
            throw new RuntimeException("Gemini 응답 파싱 실패 (JSON 형식 오류): " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 중 예상치 못한 오류 발생: 원본 JSON: {}", json, e);
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
        }
    }


    @Transactional
    public SaveWalkRouteResponse saveSelectedRoute(Long userId, WalkRouteSelectionRequest req) {
        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자ID " + userId+"를 찾을 수 없습니다"));

        //경로값 존재 확인
        WalkRouteResponse routeDto = req.getSelectedRoute();
        if (routeDto == null || routeDto.getWaypoints() == null || routeDto.getWaypoints().isEmpty()) {
            throw new IllegalArgumentException("선택 경로가 비어있습니다(waypoints 없음)");
        }
        //기관 검증
        Organization organization = getAndValidateOrganization(userId, req.getOrgId());

        // 저장
        WalkRecommendation entity = WalkRecommendation.builder()
                .user(user)
                .organization(organization)
                .groupId(req.getGroupId())
                .description(routeDto.getDescription())
                .plannedMinutes(routeDto.getEstimatedTime())
                .wayPoints(routeDto.getWaypoints())
                .build();

        WalkRecommendation savedRoute = walkRecommendationRepository.save(entity);

        return SaveWalkRouteResponse.builder()
                .walkRecommendationId(savedRoute.getId())
                .message("산책경로가 성공적으로 저장되었습니다")
                .savedAt(savedRoute.getCreatedAt())
                .build();
    }

    private Organization getAndValidateOrganization(Long userId, Long orgId) {
        if (orgId == null) {
            return null;
        }

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("기관 ID " + orgId + "를 찾을 수 없습니다."));

        boolean linked = userOrganizationRepository
                .existsByUser_IdAndOrganization_Id(userId, orgId);

        if (!linked) {
            throw new IllegalArgumentException("해당 기관(ID: " + orgId + ")은 사용자가 저장한 기관이 아닙니다. 권한 없음.");
        }
        return organization;
    }
}
