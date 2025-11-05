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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WalkRecommendationService {

    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final WalkRecommendationRepository walkRecommendationRepository;

    public WalkRecommendationGroupResponse recommendRoutes(Long orgId, int minutes, double currentLat,double currentLng) {

        String prompt;
        Long organizationIdForResponse = null;

        //기관 ID가 있을 때
        if(orgId != null) {
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다"));
            prompt = buildPromptWithOrg(org, minutes);
            organizationIdForResponse = org.getId();
        }else { // 기관 ID가 없을 때
            prompt = buildPromptGeneral(currentLat, currentLng, minutes);
        }

        String geminiResult = geminiService.generateRoutesFromGemini(prompt);

        // JSON 파싱해서 DTO 변환
        List<WalkRouteResponse> routes = parseRoutes(geminiResult);

        return WalkRecommendationGroupResponse.builder()
                .recommendationGroupId(UUID.randomUUID().toString())
                .orgId(organizationIdForResponse)
                .routes(routes)
                .build();

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
                -코스의 설명을 추천코스의 특징을 잘 살리면서, 산책을 하고 싶다는 욕구를 불어일으키는 문구로 넣어주세요.
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

            JsonNode routesNode = node.get("routes");
            if (routesNode == null || !routesNode.isArray()) {
                // "routes" 필드가 없거나 배열이 아니면 유효하지 않은 응답으로 처리
                throw new IllegalArgumentException("Gemini 응답 JSON에 'routes' 배열이 없습니다.");
            }

            for (JsonNode routeNode : routesNode) {
                if (routeNode.get("id") == null || routeNode.get("description") == null || routeNode.get("waypoints") == null || routeNode.get("estimatedTime") == null) {
                    System.err.println("경로 항목에서 필수 필드가 누락되었습니다: " + routeNode.toString());
                    continue;
                }

                List<Waypoint> waypoints = new ArrayList<>();
                JsonNode waypointsNode = routeNode.get("waypoints");
                if(waypointsNode.isArray()) {
                    for (JsonNode wp : waypointsNode) {
                        if (wp.get("lat") != null && wp.get("lng") != null) {
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
        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패: " +json +"/ 에러: "+ e.getMessage(), e);
        }
    }

    @Transactional
    public SaveWalkRouteResponse saveSelectedRoute(Long userId, WalkRouteSelectionRequest req) {
        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        //경로값 존재 확인
        var routeDto = req.getSelectedRoute();
        if (routeDto == null || routeDto.getWaypoints() == null || routeDto.getWaypoints().isEmpty()) {
            throw new IllegalArgumentException("선택 경로가 비어있습니다(waypoints 없음).");
        }

        //기관 검증
        Organization organization = null; // 미리초기화
        if(req.getOrgId() != null){
            Long orgId = req.getOrgId();
            organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new IllegalArgumentException("기관을 찾을 수 없습니다"));

            boolean linked = userOrganizationRepository
                    .existsByUser_IdAndOrganization_Id(userId,orgId);

            if(!linked){
                throw new IllegalArgumentException("해당 기관(ID: " + orgId + ")은 사용자가 저장한 기관이 아닙니다. 권한 없음.");
            }
        }


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
}
