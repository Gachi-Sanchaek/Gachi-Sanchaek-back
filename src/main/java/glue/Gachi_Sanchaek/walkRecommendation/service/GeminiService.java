package glue.Gachi_Sanchaek.walkRecommendation.service;

import glue.Gachi_Sanchaek.walkRecommendation.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
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

import static java.rmi.server.LogStream.log;

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

    @jakarta.annotation.PostConstruct
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
       Map<String, Object> requestBody = Map.of(
               "contents", List.of(
                       Map.of("parts", List.of(Map.of("text", prompt)))
               ),
               "generationConfig", Map.of(
                       "responseMimeType", "application/json"
               )
       );

       GeminiResponse res = webClient.post()
               .uri(uriBuilder -> uriBuilder
                       .path("/v1beta/models/"+model+":generateContent")
                       .build()
               )
               .bodyValue(requestBody)
               .retrieve()
               .onStatus(HttpStatusCode::isError, r ->
                    r.bodyToMono(String.class).flatMap(body ->
                       Mono.error(new RuntimeException("Gemini Error " + r.statusCode() + ": " + body)))
               )
               .bodyToMono(GeminiResponse.class)
               .block();

       if (res == null || res.getCandidates() == null || res.getCandidates().isEmpty()) {
               throw new RuntimeException("Gemini 응답이 비어 있습니다.");
           }


       return res.getCandidates()
               .get(0)
               .getContent()
               .getParts()
               .get(0)
               .getText();

   }

}
