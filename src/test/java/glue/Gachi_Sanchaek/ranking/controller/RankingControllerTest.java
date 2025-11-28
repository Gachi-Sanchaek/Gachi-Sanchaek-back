package glue.Gachi_Sanchaek.ranking.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import glue.Gachi_Sanchaek.domain.ranking.controller.RankingController;
import glue.Gachi_Sanchaek.domain.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.domain.ranking.service.RankingService;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
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

@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService; // MockBean 서비스 변경

    private CustomUserDetails mockUserDetails;
    private Authentication mockAuthentication;
    private Long testUserId = 1L;
    private Integer testPeriod = 202401;

    private RankingResponseDto rank1Dto;
    private RankingResponseDto rank2Dto;
    private RankingResponseDto myRankDto;

    @BeforeEach
    void setUp() {
        mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUserId()).thenReturn(testUserId);

        mockAuthentication = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        rank1Dto = mock(RankingResponseDto.class);
        when(rank1Dto.getRanking()).thenReturn(1L);
        when(rank1Dto.getNickname()).thenReturn("Ranker 1");
        when(rank1Dto.getPoint()).thenReturn(1000L);

        rank2Dto = mock(RankingResponseDto.class);
        when(rank2Dto.getRanking()).thenReturn(2L);
        when(rank2Dto.getNickname()).thenReturn("Ranker 2");
        when(rank2Dto.getPoint()).thenReturn(900L);

        myRankDto = mock(RankingResponseDto.class);
        when(myRankDto.getRanking()).thenReturn(50L);
        when(myRankDto.getNickname()).thenReturn("MyUser");
        when(myRankDto.getPoint()).thenReturn(100L);
    }

    @Test
    @DisplayName("getRankings (GET /api/v1/rankings) - 인증된 사용자로 Top10 랭킹 조회")
    void getRankings_withAuthenticatedUser_shouldReturnRankingList() throws Exception {
        // Given
        List<RankingResponseDto> serviceResult = List.of(rank1Dto, rank2Dto);
        when(rankingService.findTop10ByPeriod(testPeriod)).thenReturn(serviceResult);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings")
                        .param("date", testPeriod.toString())
                        .with(authentication(mockAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nickname").value("Ranker 1"))
                .andExpect(jsonPath("$.data[1].ranking").value(2));

        verify(rankingService, times(1)).findTop10ByPeriod(testPeriod);
    }

    @Test
    @DisplayName("getRankings (GET /api/v1/rankings) - 인증되지 않은 사용자는 401 응답")
    void getRankings_withUnauthenticatedUser_shouldReturn401() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/v1/rankings")
                        .param("date", testPeriod.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(rankingService, never()).findTop10ByPeriod(anyInt());
    }

    @Test
    @DisplayName("getMyRanking (GET /api/v1/rankings/my-ranking) - 인증된 사용자로 내 랭킹 조회")
    void getMyRanking_withAuthenticatedUser_shouldReturnMyRanking() throws Exception {
        // Given
        when(rankingService.findByPeriodAndId(testPeriod, testUserId)).thenReturn(myRankDto);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/my-ranking")
                        .param("date", testPeriod.toString())
                        .with(authentication(mockAuthentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.nickname").value("MyUser"))
                .andExpect(jsonPath("$.data.ranking").value(50));

        verify(rankingService, times(1)).findByPeriodAndId(testPeriod, testUserId);
    }

    @Test
    @DisplayName("getMyRanking (GET /api/v1/rankings/my-ranking) - 인증되지 않은 사용자는 401 응답")
    void getMyRanking_withUnauthenticatedUser_shouldReturn401() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/my-ranking")
                        .param("date", testPeriod.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(rankingService, never()).findByPeriodAndId(anyInt(), anyLong());
    }
}