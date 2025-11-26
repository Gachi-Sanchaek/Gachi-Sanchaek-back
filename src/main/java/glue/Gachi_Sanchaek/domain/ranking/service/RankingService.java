package glue.Gachi_Sanchaek.domain.ranking.service;

import glue.Gachi_Sanchaek.domain.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.domain.ranking.entity.Ranking;
import glue.Gachi_Sanchaek.domain.ranking.repository.RankingRepository;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import glue.Gachi_Sanchaek.common.util.DateUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final UserService userService;

    public List<RankingResponseDto> findTop10ByPeriod(int period) {
        return rankingRepository.findTop10ByPeriod(period);
    }

    public RankingResponseDto findByPeriodAndId(int period, Long userId){
        return rankingRepository.findDtoByPeriodAndUserId(period, userId);
    }

    @Transactional
    public void updateRanking(Long userId, Long reward){
        int period = DateUtil.getTodayYYYYMMW();
        Ranking ranking = rankingRepository.findByRankPeriodAndUserId(period, userId)
                .orElseGet(()->{
                    User user = userService.findById(userId);
                    return new Ranking(user, 0L, period);
                });
        ranking.addPoint(reward);
        rankingRepository.save(ranking);
    }
}
