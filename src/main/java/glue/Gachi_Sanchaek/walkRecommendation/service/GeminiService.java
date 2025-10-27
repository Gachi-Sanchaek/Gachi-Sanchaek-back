package glue.Gachi_Sanchaek.walkRecommendation.service;

import glue.Gachi_Sanchaek.walkRecommendation.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.base-url}")
    private String geminiBaseUrl;

   private WebClient webClient;

    @jakarta.annotation.PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.create(geminiBaseUrl);
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
                       .path("/v1beta/models/gemini-2.0-flash:generateContent")
                       .queryParam("key",geminiApiKey)
                       .build())
               .bodyValue(requestBody)
               .retrieve()
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
