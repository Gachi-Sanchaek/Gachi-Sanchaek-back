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
import java.util.List;
import java.util.Map;

@Service
public class GeminiWalkService {

    private final WebClient webClient;
    private final String modelName;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public GeminiWalkService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model-name}") String modelName,
            ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        this.modelName = modelName;
        this.apiKey = apiKey;

        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public int countTrashImage(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String mimeType = image.getContentType();
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "image/png"; // Defaulting to PNG
            }

            System.out.println("DEBUG MIME Type: " + mimeType);
            System.out.println("DEBUG File Size (bytes): " + imageBytes.length);

            Map<String, Object> requestMap = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(
                                            Map.of("text", "이 이미지에서 쓰레기(병, 캔, 비닐봉지 등)의 개수를 세어주세요. " +
                                                    "오직 JSON 형식으로만 응답해야 하며, 예시는 {\\\"trashCount\\\": 3} 입니다."),
                                            Map.of("inline_data", Map.of(
                                                    "mime_type", mimeType,
                                                    "data", base64Image
                                            ))
                                    )
                            )
                    )
            );

            String requestBody = objectMapper.writeValueAsString(requestMap);

            String urlWithApiKey = String.format("models/%s:generateContent?key=%s", modelName, apiKey);

            String responseBody = webClient.post()
                    .uri(urlWithApiKey)
                    .bodyValue(requestBody)
                    .retrieve()

                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("Gemini API Error Response: " + errorBody);
                                    return Mono.error(new RuntimeException("Gemini API 호출 실패: " + errorBody));
                                });
                    })
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isMissingNode() || candidates.size() == 0) {
                System.err.println("Gemini API 응답에 candidates 필드가 없습니다. 응답: " + responseBody);
                return 0;
            }

            String text = candidates.get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            String cleanText = text.replaceAll("```json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            System.out.println("Gemini Raw Text Response (clean): " + cleanText);

            JsonNode parsed = objectMapper.readTree(cleanText);
            return parsed.path("trashCount").asInt(0);

        } catch (IOException e) {
            System.err.println("이미지 처리 중 오류: " + e.getMessage());
            return 0;
        } catch (Exception e) {
            System.err.println("Gemini API 호출 오류 또는 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}