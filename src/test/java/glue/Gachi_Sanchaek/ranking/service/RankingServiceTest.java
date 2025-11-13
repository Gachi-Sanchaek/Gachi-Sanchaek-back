package glue.Gachi_Sanchaek.ranking.service;

import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.entity.Ranking;
import glue.Gachi_Sanchaek.ranking.repository.RankingRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private RankingRepository rankingRepository;

    @Mock
    private UserService userService;

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .nickname("testUser")
                .build();
    }

    @Test
    @DisplayName("기존 랭킹이 있을 경우: 점수가 합산되고 저장된다")
    void updateRanking_WhenRankingExists() {
        // given
        Long userId = 1L;
        Long reward = 100L;
        User user = createUser(userId);

        Ranking existingRanking = new Ranking(user, 50L, 202401);

        given(rankingRepository.findByRankPeriodAndUserId(anyInt(), eq(userId)))
                .willReturn(Optional.of(existingRanking));

        // when
        rankingService.updateRanking(userId, reward);

        // then
        assertThat(existingRanking.getPoint()).isEqualTo(150L);
        verify(rankingRepository).save(existingRanking);
        verify(userService, never()).findById(any());
    }

    @Test
    @DisplayName("기존 랭킹이 없을 경우: 유저를 조회하고 새 랭킹을 생성하여 저장한다")
    void updateRanking_WhenRankingDoesNotExist() {
        // given
        Long userId = 1L;
        Long reward = 200L;
        User user = createUser(userId);

        given(rankingRepository.findByRankPeriodAndUserId(anyInt(), eq(userId)))
                .willReturn(Optional.empty());

        given(userService.findById(userId)).willReturn(user);

        // when
        rankingService.updateRanking(userId, reward);

        // then
        ArgumentCaptor<Ranking> rankingCaptor = ArgumentCaptor.forClass(Ranking.class);
        verify(rankingRepository).save(rankingCaptor.capture());

        Ranking savedRanking = rankingCaptor.getValue();

        assertThat(savedRanking.getUser()).isEqualTo(user);
        assertThat(savedRanking.getPoint()).isEqualTo(200L);
        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("Top10 조회 요청 시 Repository를 호출한다")
    void findTop10ByPeriod() {
        // given
        int period = 202401;
        List<RankingResponseDto> expectedList = List.of();

        given(rankingRepository.findTop10ByPeriod(period)).willReturn(expectedList);

        // when
        List<RankingResponseDto> result = rankingService.findTop10ByPeriod(period);

        // then
        assertThat(result).isEqualTo(expectedList);
        verify(rankingRepository).findTop10ByPeriod(period);
    }

    @Test
    @DisplayName("특정 유저 랭킹 조회 시 Repository를 호출한다")
    void findByPeriodAndId() {
        // given
        int period = 202401;
        Long userId = 1L;
        RankingResponseDto mockDto = org.mockito.Mockito.mock(RankingResponseDto.class);


        given(rankingRepository.findDtoByPeriodAndUserId(period, userId))
                .willReturn(mockDto);

        // when
        RankingResponseDto result = rankingService.findByPeriodAndId(period, userId);

        // then
        assertThat(result).isEqualTo(mockDto);
    }
}