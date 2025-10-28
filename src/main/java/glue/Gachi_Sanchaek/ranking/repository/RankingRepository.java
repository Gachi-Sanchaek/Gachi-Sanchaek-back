package glue.Gachi_Sanchaek.ranking.repository;

import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.entity.Ranking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    @Query(value = """
        SELECT 
            u.nickname AS nickname,
            u.profile_image_url AS profileImageUrl,
            r.point AS point,
            RANK() OVER (ORDER BY r.point DESC) AS ranking
        FROM rankings r
        JOIN users u ON u.id = r.user_id
        WHERE r.rank_period = :period
        ORDER BY r.point DESC
        LIMIT 10
        """, nativeQuery = true)
    List<RankingResponseDto> findTop10ByPeriod(@Param("period") int period);

    @Query(value = """
        SELECT 
            u.nickname AS nickname,
            u.profile_image_url AS profileImageUrl,
            r.point AS point,
            RANK() OVER (ORDER BY r.point DESC) AS ranking
        FROM rankings r
        JOIN users u ON u.id = r.user_id
        WHERE r.rank_period = :period AND u.id = :userId
    """, nativeQuery = true)
    RankingResponseDto findByPeriodAndUserId(@Param("period") int period, @Param("userId") Long userId);

    // for update
    @Query("SELECT r FROM Ranking r WHERE r.rankPeriod = :period AND r.user.id = :userId")
    Ranking findByPeriodAndUserIdEntity(@Param("period") int period, @Param("userId") Long userId);
}
