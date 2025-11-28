package glue.Gachi_Sanchaek.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import glue.Gachi_Sanchaek.common.security.config.SecurityConfig;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.common.security.jwt.JWTPayload;
import glue.Gachi_Sanchaek.domain.user.controller.UserController;
import glue.Gachi_Sanchaek.domain.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.domain.user.dto.UserUpdateRequestDto;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    private CustomUserDetails mockUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        objectMapper.registerModule(new JavaTimeModule());

        mockUserDetails = new CustomUserDetailsStub(1L, "USER");
        testUser = User.builder()
                .id(1L)
                .nickname("기존닉네임")
                .kakaoId(12345L)
                .gender("MALE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /me : 내 정보 조회 성공")
    void getMyInfo_Success() throws Exception {
        // given
        when(userService.findById(1L)).thenReturn(testUser);

        // when & then
        mockMvc.perform(get("/api/v1/users/me")
                        .with(user(mockUserDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("기존닉네임"));
    }

    @Test
    @DisplayName("POST / : 신규 회원 추가 정보(가입) 성공")
    void join_Success() throws Exception {
        // given
        UserJoinRequestDto requestDto = new UserJoinRequestDto("새닉네임", "MALE");
        User joinedUser = User.builder()
                .id(1L)
                .nickname("새닉네임")
                .gender("MALE")
                .build();

        when(userService.completeRegistration(eq(1L), any(UserJoinRequestDto.class))).thenReturn(joinedUser);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("PATCH /me : 내 정보 수정 성공")
    void updateMyInfo_Success() throws Exception {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("새닉네임", "http://new.img/profile.png");
        User updatedUser = User.builder()
                .id(1L)
                .nickname("새닉네임")
                .profileImageUrl("http://new.img/profile.png")
                .build();

        when(userService.update(eq(1L), any(UserUpdateRequestDto.class))).thenReturn(updatedUser);

        // when & then
        mockMvc.perform(patch("/api/v1/users/me")
                        .with(user(mockUserDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("http://new.img/profile.png"));
    }

    @Test
    @DisplayName("GET /check-nickname : 닉네임 사용 가능 (true)")
    void checkNickname_Available() throws Exception {
        // given
        String nickname = "새닉네임";
        when(userService.isAvailableNickname(nickname)).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/users/check-nickname")
                        .with(user(mockUserDetails))
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.isAvailable").value(true));
    }

    @Test
    @DisplayName("GET /check-nickname : 닉네임 중복 (false)")
    void checkNickname_Duplicate() throws Exception {
        // given
        String nickname = "중복닉네임";
        when(userService.isAvailableNickname(nickname)).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/users/check-nickname")
                        .with(user(mockUserDetails))
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isAvailable").value(false));
    }


    // --- 테스트용 CustomUserDetails 스텁(Stub) ---
    private static class CustomUserDetailsStub extends CustomUserDetails {

        public CustomUserDetailsStub(Long userId, String role) {
            super(createMockPayload(userId, role));
        }

        private static JWTPayload createMockPayload(Long userId, String role) {
            JWTPayload mockPayload = Mockito.mock(JWTPayload.class);
            when(mockPayload.getUserId()).thenReturn(userId);
            when(mockPayload.getRole()).thenReturn(role);
            return mockPayload;
        }
    }
}

