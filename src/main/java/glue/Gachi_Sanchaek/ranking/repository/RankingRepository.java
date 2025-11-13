package glue.Gachi_Sanchaek.ranking.repository;

import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.entity.Ranking;
import java.util.List;
import java.util.Optional; // 1. Optional import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    String CTE_RANKING = """
        WITH RankedUsers AS (
            SELECT 
                r.user_id,
                u.nickname,
                u.profile_image_url AS profileImageUrl,
                r.point,
                r.updated_at AS updatedAt,
                RANK() OVER (ORDER BY r.point DESC) AS ranking
            FROM rankings r
            JOIN users u ON u.id = r.user_id
            WHERE r.rank_period = :period
        )
    """;


    @Query(value = CTE_RANKING + """
        SELECT nickname, profileImageUrl, point, ranking
        FROM RankedUsers
        ORDER BY ranking ASC, updatedAt asc
        LIMIT 10
        """, nativeQuery = true)
    List<RankingResponseDto> findTop10ByPeriod(@Param("period") int period);


    @Query(value = CTE_RANKING + """
        SELECT nickname, profileImageUrl, point, ranking
        FROM RankedUsers
        WHERE user_id = :userId
    """, nativeQuery = true)
    RankingResponseDto findDtoByPeriodAndUserId(@Param("period") int period, @Param("userId") Long userId);



    Optional<Ranking> findByRankPeriodAndUserId(int period, Long userId);
}