package glue.Gachi_Sanchaek.walkRecommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.walkRecommendation.dto.*;
import glue.Gachi_Sanchaek.walkRecommendation.entity.WalkRecommendation;
import glue.Gachi_Sanchaek.walkRecommendation.repository.WalkRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WalkRecommendationService {

    private final OrganizationRepository organizationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final WalkRecommendationRepository walkRecommendationRepository;

    public WalkRecommendationGroupResponse recommendRoutes(Long orgId, int minutes, double currentLat,double currentLng) {

        String prompt;
        //기관 ID가 있을 때
        if(orgId != null) {
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다"));
            prompt = buildPromptWithOrg(org, minutes);
        }else { // 기관 ID가 없을 때
            prompt = buildPromptGeneral(currentLat, currentLng, minutes);
        }

        String geminiResult = geminiService.generateRoutesFromGemini(prompt);

        // JSON 파싱해서 DTO 변환
        List<WalkRouteResponse> routes = parseRoutes(geminiResult);

        WalkRecommendationGroupResponse group = WalkRecommendationGroupResponse.builder()
                .recommendationGroupId(UUID.randomUUID().toString())
                .routes(routes)
                .build();

        return group;
    }

    private String buildPromptWithOrg(Organization org, int minutes) {
        return String.format("""
                당신은 산책 코스를 추천하는 AI입니다.
                
                [요구사항]
                -출발지와 도착지는 모두 "%s"(위도: %f, 경도: %f) 입니다.
                -왕복 기준 %d분 동안 걸을 수 있는 경로를 3개 추천해주세요.
                -각 코스마다 15글자 이내의 설명을 넣어주세요.
                -한 코스에서 같은 경로를 지날 수 없습니다.
                -각 코스는 서로 겹치지 않게 구성해주세요.
                -각 코스에는 id, description, estimatedTime, waypoint(lat,lng)를 포함한 JSON 형태로 응답해주세요.
                -waypoint에서 lat, lng 의 개수는 10개씩 주세요.
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
                
                """, org.getName(), org.getLatitude(), org.getLongitude(), minutes);

    }
    private String buildPromptGeneral(double currentLat, double currentLng, int minutes) {
        return String.format("""
                당신은 산책 코스를 추천하는 AI입니다.
                
                [요구사항]
                -출발지와 도착지는 모두 "현재위치"(위도: %f, 경도: %f) 입니다.
                -추천 경로는 **간단하게 주변 산책**을 할 수 있는 코스여야 합니다.
                -왕복 기준 %d분 동안 걸을 수 있는 경로를 3개 추천해주세요.
                -각 코스마다 15글자 이내의 설명을 넣어주세요.
                -한 코스에서 같은 경로를 지날 수 없습니다.
                -각 코스는 서로 겹치지 않게 구성해주세요.
                -각 코스에는 id, description, estimatedTime, waypoint(lat,lng)를 포함한 JSON 형태로 응답해주세요.
                -waypoint에서 lat, lng 의 개수는 10개씩 주세요.
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
                
                """, currentLat, currentLng, minutes);

    }

    private List<WalkRouteResponse> parseRoutes(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            List<WalkRouteResponse> routes = new ArrayList<>();

            for (JsonNode routeNode : node.get("routes")) {
                List<Waypoint> waypoints = new ArrayList<>();
                for (JsonNode wp : routeNode.get("waypoints")) {
                    waypoints.add(new Waypoint(wp.get("lat").asDouble(), wp.get("lng").asDouble()));
                }

                routes.add(WalkRouteResponse.builder()
                        .id(routeNode.get("id").asLong())
                        .description(routeNode.get("description").asText())
                        .waypoints(waypoints)
                        .estimatedTime(routeNode.get("estimatedTime").asInt())
                        .build());
            }
            return routes;
        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SaveWalkRouteResponse saveSelectedRoute(Long userId, WalkRouteSelectionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Organization organization = null; // 미리초기화
        if(req.getOrgId() != null){
            organization = organizationRepository.findById(req.getOrgId())
                    .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다"));
        }

        WalkRecommendation route = WalkRecommendation.builder()
                .user(user)
                .organization(organization)
                .groupId(req.getGroupId())
                .description(req.getSelectedRoute().getDescription())
                .plannedMinutes(req.getSelectedRoute().getEstimatedTime())
                .wayPoints(req.getSelectedRoute().getWaypoints())
                .build();

        WalkRecommendation savedRoute = walkRecommendationRepository.save(route);

        return SaveWalkRouteResponse.builder()
                .walkRecommendationId(savedRoute.getId())
                .message("산책경로가 성공적으로 저장되었습니다")
                .savedAt(savedRoute.getCreatedAt())
                .build();
    }
}
