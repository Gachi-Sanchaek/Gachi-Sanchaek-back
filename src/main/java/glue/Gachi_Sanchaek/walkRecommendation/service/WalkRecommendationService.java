package glue.Gachi_Sanchaek.walkRecommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.repository.OrganizationRepository;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRecommendationGroupResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.WalkRouteResponse;
import glue.Gachi_Sanchaek.walkRecommendation.dto.Waypoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WalkRecommendationService {

    private final OrganizationRepository organizationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WalkRecommendationGroupResponse recommendRoutes(Long orgId, int minutes) {

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다"));

        String prompt = buildPrompt(org, minutes);

        String geminiResult = geminiService.generateRoutesFromGemini(prompt);

        // JSON 파싱해서 DTO 변환
        List<WalkRouteResponse> routes = parseRoutes(geminiResult);

        WalkRecommendationGroupResponse group = WalkRecommendationGroupResponse.builder()
                .recommendationGroupId(UUID.randomUUID().toString())
                .routes(routes)
                .build();

        return group;
    }

    private String buildPrompt(Organization org, int minutes) {
        return String.format("""
                당신은 산책 코스를 추천하는 AI입니다.
                
                [요구사항]
                -출발지와 도착지는 모두 "%s"(위도: %f, 경도: %f) 입니다.
                -왕복 기준 %d분 동안 걸을 수 있는 경로를 3개 추천해주세요.
                -각 코스마다 15글자 이내의 설명을 넣어주세요.
                -한 코스에서 같은 경로를 지날 수 없습니다.
                -각 코스는 서로 겹치지 않게 구성해주세요.
                -각 코스에는 id, description, estimatedTime, waypoint(lat,lng)를 포함한 JSON 형태로 응답해주세요.
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
}
