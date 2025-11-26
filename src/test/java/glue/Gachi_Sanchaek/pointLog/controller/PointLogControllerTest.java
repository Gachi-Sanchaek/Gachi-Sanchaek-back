package glue.Gachi_Sanchaek.pointLog.controller;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.domain.pointLog.controller.PointLogController;
import glue.Gachi_Sanchaek.domain.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.domain.pointLog.entity.PointLog;
import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.domain.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.common.util.ApiResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointLogController.class)
class PointLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointLogService pointLogService;

    private CustomUserDetails mockUserDetails;
    private Authentication mockAuthentication;
    private PointLogResponseDto testPointLogDto;
    private PointLog testPointLog;
    private User testUser;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUserId()).thenReturn(testUserId);

        mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        testUser = User.builder().build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        testPointLog = PointLog.builder()
                .id(1L)
                .user(testUser)
                .amount(100L)
                .type(WalkType.DOG)
                .location("테스트 위치")
                .build();

        testPointLogDto = new PointLogResponseDto(testPointLog);
    }

    @Test
    @DisplayName("getAllPointLogs (GET /api/v1/pointLog) - 인증된 사용자로 포인트 로그 조회")
    void getAllPointLogs_withAuthenticatedUser_shouldReturnPointLogList() throws Exception {
        // Given
        List<PointLogResponseDto> serviceResult = List.of(testPointLogDto);

        when(pointLogService.findByUserId(testUserId)).thenReturn(serviceResult);

        ApiResponse<List<PointLogResponseDto>> expectedBody = ApiResponse.ok(serviceResult).getBody();

        String expectedJson = objectMapper.writeValueAsString(expectedBody);

        // When & Then
        mockMvc.perform(get("/api/v1/pointLog")
                        .with(authentication(mockAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(pointLogService, times(1)).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getAllPointLogs (GET /api/v1/pointLog) - 인증되지 않은 사용자는 401 응답")
    void getAllPointLogs_withUnauthenticatedUser_shouldReturn401() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/v1/pointLog")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(pointLogService, never()).findByUserId(anyLong());
    }
}