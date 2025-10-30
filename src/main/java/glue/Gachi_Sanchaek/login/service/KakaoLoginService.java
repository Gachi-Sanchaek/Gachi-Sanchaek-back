package glue.Gachi_Sanchaek.login.service;

import glue.Gachi_Sanchaek.login.dto.KakaoTokenResponseDto;
import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class KakaoLoginService {

    private static final String AUTH_TOKEN_PATH = "/oauth/token";
    private static final String USER_INFO_PATH = "/v2/user/me";
    private static final String CONTENT_TYPE_FORM_URLENCODED = HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString();

    private final WebClient kAuthWebClient;
    private final WebClient kApiWebClient;

    private final String clientId;
    private final String redirectUri;

    public KakaoLoginService(
            @Value("${kakao.client_id}") String clientId,
            @Value("${kakao.redirect_uri}") String redirectUri,
            @Value("${kakao.url.auth}") String kAuthUrl,
            @Value("${kakao.url.api}") String kApiUrl
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;

        this.kAuthWebClient = WebClient.builder()
                .baseUrl(kAuthUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED)
                .build();

        this.kApiWebClient = WebClient.builder()
                .baseUrl(kApiUrl)
                .build();
    }

    public String getAccessTokenFromKakao(String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = kAuthWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(AUTH_TOKEN_PATH)
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code", code)
                        .build(true))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new IllegalArgumentException("Kakao Auth 4xx Error: " + errorBody))
                        ))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("Kakao Auth 5xx Error: " + errorBody))
                        ))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        log.info("[Kakao] Code Validation Complete");
        System.out.println("Access Token :" + kakaoTokenResponseDto.getAccessToken());

        return kakaoTokenResponseDto.getAccessToken();
    }

    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {

        KakaoUserInfoResponseDto userInfo = kApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_INFO_PATH)
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new IllegalArgumentException("Kakao API 4xx Error: " + errorBody))
                        )
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("Kakao API 5xx Error: " + errorBody))
                        )
                )
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        log.info("[Kakao] user info validation Complete : {} / {}", userInfo.getId(), userInfo.getKakaoAccount().getProfile().getNickName());

        return userInfo;
    }
}