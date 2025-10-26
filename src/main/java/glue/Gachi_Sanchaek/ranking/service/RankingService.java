package glue.Gachi_Sanchaek.ranking.service;

import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.entity.Ranking;
import glue.Gachi_Sanchaek.ranking.repository.RankingRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.util.DateUtil;
import java.time.LocalDateTime;
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
        return rankingRepository.findByPeriodAndUserId(period, userId);
    }

    @Transactional
    public void updateRanking(Long userId, Long reward){
        int period = DateUtil.getTodayYYYYMMW();
        User user = userService.findById(userId);
        Ranking ranking = rankingRepository.findByPeriodAndUserIdEntity(period, userId);

        if (ranking != null) {
            ranking.setPoint(ranking.getPoint() + reward);
        } else {
            ranking = new Ranking(user, reward, period);
        }

        rankingRepository.save(ranking);
    }
}
