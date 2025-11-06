package glue.Gachi_Sanchaek.walk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Base64;

@Service
public class GeminiWalkService {

    private final WebClient webClient;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public GeminiWalkService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model-name}") String modelName,
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        this.modelName = modelName;

        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public int countTrashImage(MultipartFile image) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            String requestBody = """
                {
                  "contents": [
                    {
                      "role": "user",
                      "parts": [
                        {
                          "text": "이 이미지에서 쓰레기(병, 캔, 비닐봉지 등)의 개수를 세어주세요. " +
                                  "오직 JSON 형식으로만 응답해야 하며, 예시는 {\\\"trashCount\\\": 3} 입니다."
                        },
                        {
                          "inline_data": {
                            "mime_type": "%s",
                            "data": "%s"
                          }
                        }
                      ]
                    }
                  ]
                }
                """.formatted(image.getContentType(), base64Image);

            String url = String.format("models/%s:generateContent", modelName);
            String responseBody = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            clientResponse -> Mono.error(new RuntimeException("Gemini API 호출 실패")))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            JsonNode parsed = objectMapper.readTree(text);
            return parsed.path("trashCount").asInt(0);

        } catch (IOException e) {
            System.err.println("이미지 처리 중 오류: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            System.err.println("Gemini API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}