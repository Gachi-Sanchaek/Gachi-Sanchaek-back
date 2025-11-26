package glue.Gachi_Sanchaek.login.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.service.LoginService;
import glue.Gachi_Sanchaek.security.CustomAccessDeniedHandler;
import glue.Gachi_Sanchaek.security.CustomAuthenticationEntryPoint;
import glue.Gachi_Sanchaek.security.config.SecurityConfig;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = LoginController.class)
@Import(SecurityConfig.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService loginService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @MockBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private final String ACCESS_TOKEN_HEADER = "Authorization";
    private final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private CustomUserDetails mockUserDetails;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUserId()).thenReturn(1L);

        mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("GET /kakao/login - 카카오 로그인 성공 (신규/기존 유저)")
    void callback_Success() throws Exception {
        // given
        String testCode = "test-kakao-code";
        String mockAccessToken = "mock-access-token";
        String mockRefreshToken = "mock-refresh-token";

        User testUser = Mockito.mock(User.class);
        when(testUser.getNickname()).thenReturn("TestUser");

        LoginResponseDto mockLoginResponse = new LoginResponseDto(true, testUser);
        LoginService.AuthResult mockAuthResult = new LoginService.AuthResult(
                mockLoginResponse,
                mockAccessToken,
                mockRefreshToken
        );

        when(loginService.loginWithKakaoCode(testCode)).thenReturn(mockAuthResult);

        // when & then
        mockMvc.perform(get("/api/v1/auth/kakao/login")
                        .param("code", testCode))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_TOKEN_HEADER, "Bearer " + mockAccessToken))
                .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, mockRefreshToken));
    }

    @Test
    @DisplayName("POST /refresh - 토큰 재발급 성공")
    void refreshAccessToken_Success() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, oldRefreshToken);

        when(loginService.reissueAccessToken(oldRefreshToken)).thenReturn(newAccessToken);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshTokenCookie)
                        .with(csrf())
                        .with(authentication(mockAuthentication)))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_TOKEN_HEADER, "Bearer " + newAccessToken));
    }

    @Test
    @DisplayName("POST /refresh - 리프레시 토큰 누락 시 400 Bad Request")
    void refreshAccessToken_MissingCookie() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .with(authentication(mockAuthentication)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /refresh - 리프레시 토큰이 비어있으면 400 예외 발생")
    void refreshAccessToken_EmptyCookie() throws Exception {
        // given
        Cookie emptyCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(emptyCookie)
                        .with(csrf())
                        .with(authentication(mockAuthentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Refresh token is missing."));
    }
}

