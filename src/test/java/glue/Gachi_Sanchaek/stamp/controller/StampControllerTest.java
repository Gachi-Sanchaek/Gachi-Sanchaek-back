package glue.Gachi_Sanchaek.stamp.controller;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.domain.stamp.controller.StampController;
import glue.Gachi_Sanchaek.domain.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.domain.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.domain.stamp.service.StampService;
import glue.Gachi_Sanchaek.common.util.ApiResponse;
import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StampController.class)
class StampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StampService stampService;

    private CustomUserDetails mockUserDetails;
    private Authentication mockAuthentication;
    private StampResponseDto testStampDto;
    private Stamp testStamp;

    @BeforeEach
    void setUp() {
        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUserId()).thenReturn(1L);

        mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        testStamp = Stamp.builder()
                .id(1L)
                .name("테스트 스탬프")
                .imageUrl("test.png")
                .price(500L)
                .createdAt(LocalDateTime.now())
                .build();

        testStampDto = new StampResponseDto(testStamp);
        testStampDto.checkActivable(1000L);
    }

    @Test
    @DisplayName("findAllStamps (GET /api/v1/stamps) - 인증된 사용자로 스탬프 목록 조회 성공")
    void findAllStamps_withAuthenticatedUser_shouldReturnStampList() throws Exception {
        // Given
        Long userId = 1L;
        List<StampResponseDto> serviceResult = List.of(testStampDto);

        when(stampService.getAllStampsWithUserStatus(userId)).thenReturn(serviceResult);

        ApiResponse<List<StampResponseDto>> expectedResponse = ApiResponse.ok(serviceResult).getBody();
        String expectedJson = objectMapper.writeValueAsString(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/stamps")
                        .with(authentication(mockAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(stampService).getAllStampsWithUserStatus(userId);
    }

    @Test
    @DisplayName("findAllStamps (GET /api/v1/stamps) - 인증되지 않은 사용자는 401 응답")
    void findAllStamps_withUnauthenticatedUser_shouldReturn401() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/v1/stamps")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(stampService, never()).getAllStampsWithUserStatus(anyLong());
    }
}