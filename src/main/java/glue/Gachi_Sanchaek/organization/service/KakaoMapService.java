package glue.Gachi_Sanchaek.organization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.exception.KakaoMapApiException;
import glue.Gachi_Sanchaek.organization.dto.KakaoPlaceResponse;
import glue.Gachi_Sanchaek.organization.dto.OrganizationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.api.base-url}")
    private String kakaoBaseUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public List<OrganizationDTO> searchNearbyOrganizations(double latitude, double longitude, int radius, String keyword) {

        try {
            String rawResponse = fetchKakaoRawResponse(latitude, longitude, radius, keyword);
            return parseAndMapResponse(rawResponse, keyword);
        } catch (KakaoMapApiException e) {
            // 카카오 API 응답 4xx/5xx 에러
            throw e;
        } catch (IOException e) {
            log.error("카카오 API 응답 파싱 중 오류 발생", e);
            throw new RuntimeException("카카오 API 응답 파싱 중 오류 발생", e);
        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류 발생: ", e);
            throw new RuntimeException("카카오 API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String fetchKakaoRawResponse(double latitude, double longitude, int radius, String keyword) {
        WebClient webClient = webClientBuilder.baseUrl(kakaoBaseUrl).build();

        return webClient.get()
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
                                        new KakaoMapApiException((org.springframework.http.HttpStatus)res.statusCode(),
                                                "Kakao 4xx error: " + body))
                                )
                )
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                        res.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new KakaoMapApiException((org.springframework.http.HttpStatus)res.statusCode(),
                                                "Kakao 5xx error: " + body))
                                )
                )
                .bodyToMono(String.class)
                .block();
    }

    private List<OrganizationDTO> parseAndMapResponse(String rawResponse, String keyword) throws IOException {
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
    }
}
