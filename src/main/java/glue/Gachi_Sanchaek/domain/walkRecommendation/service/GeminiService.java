package glue.Gachi_Sanchaek.domain.walkRecommendation.service;

import glue.Gachi_Sanchaek.common.exception.GeminiServerOverloadException;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.GeminiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.base-url}")
    private String geminiBaseUrl;

    @Value("${gemini.model-name}")
    private String model;

   private WebClient webClient;

    @PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Goog-Api-Key", geminiApiKey)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(90))
                                .compress(true)
                ))
                .build();
    }

   public String generateRoutesFromGemini(String prompt) {
       Map<String, Object> requestBody = buildRequestBody(prompt);

       return webClient.post()
               .uri(uriBuilder -> uriBuilder
                       .path("/v1beta/models/" + model + ":generateContent")
                       .build()
               )
               .bodyValue(requestBody)
               .retrieve()
               .onStatus(HttpStatusCode::isError, r ->
                       r.bodyToMono(String.class).flatMap(body -> {
                           log.error("Gemini API Error {}: {}", r.statusCode(), body);
                           // 5xx 서버 과부화 재시도 가능 예외처리
                           if (r.statusCode().is5xxServerError()) {
                               return Mono.error(new GeminiServerOverloadException("Gemini Server Error " + r.statusCode() + ": " + body));
                           }
                           // 4xx 기타에러는 재시도 X
                           return Mono.error(new RuntimeException("Gemini Error " + r.statusCode() + ": " + body));
                       })
               )
               .bodyToMono(GeminiResponse.class)
               .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(2)) // 최대 3번, 2초 간격으로 재시도
                       // GeminiServerOverloadException일 때 재시도
                       .filter(throwable -> throwable instanceof GeminiServerOverloadException)
                       .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                           log.error("Gemini API is unavailable after 3 retries.", retrySignal.failure());
                           return new RuntimeException("Gemini API가 여러 번의 재시도 후에도 응답하지 않습니다", retrySignal.failure());
                       }))
               .block()
               .getCandidates().stream()
               .findFirst()
               .map(candidate -> candidate.getContent().getParts().get(0).getText())
               .orElseThrow(() -> new RuntimeException("Gemini 응답에서 유효한 텍스트를 찾을 수 없습니다."));
   }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );
    }

}
