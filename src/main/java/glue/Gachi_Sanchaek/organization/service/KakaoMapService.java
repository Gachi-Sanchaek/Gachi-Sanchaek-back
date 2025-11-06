package glue.Gachi_Sanchaek.organization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.exception.KakaoMapApiException;
import glue.Gachi_Sanchaek.organization.dto.KakaoPlaceResponse;
import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.api.base-url}")
    private String kakaoBaseUrl;

    private final WebClient.Builder webClientBuilder;

    public List<OrganizationDTO> searchNearbyOrganizations(double latitude, double longitude, int radius, String keyword){

    try{
        WebClient webClient = webClientBuilder.baseUrl(kakaoBaseUrl).build();

        String rawResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", radius)
                        .queryParam("size", 10)
                        .build(true))
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                        res.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new KakaoMapApiException(HttpStatus.BAD_GATEWAY,
                                                "Kakao 4xx error: " + body))
                                )
                )
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                        res.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new KakaoMapApiException(HttpStatus.BAD_GATEWAY,
                                                "Kakao 5xx error: " + body))
                                )
                )
                .bodyToMono(String.class)
                .block();



        ObjectMapper objectMapper = new ObjectMapper();
        KakaoPlaceResponse response = objectMapper.readValue(rawResponse, KakaoPlaceResponse.class);

        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            System.out.println(" 카카오에서 결과를 찾을 수 없습니다. (query=" + keyword + ")");
            return List.of();
        }

        return response.getDocuments().stream()
                .map(doc -> OrganizationDTO.builder()
                        .kakaoId(Long.parseLong(doc.getKakaoId()))
                        .name(doc.getName())
                        .phone(doc.getPhone() == null || doc.getPhone().isEmpty() ? null : doc.getPhone())
                        .address(doc.getAddress())
                        .latitude(Double.parseDouble(doc.getLatitude()))
                        .longitude(Double.parseDouble(doc.getLongitude()))
                        .distance(Integer.parseInt(doc.getDistance() == null ? "0" : doc.getDistance()))
                        .build())
                .collect(Collectors.toList());
    } catch (KakaoMapApiException e){
        throw e;
    }
    catch (Exception e) {
        throw new RuntimeException("카카오 API 호출 중 오류 발생: " + e.getMessage());
    }

    }
}
