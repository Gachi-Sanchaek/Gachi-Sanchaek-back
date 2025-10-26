package glue.Gachi_Sanchaek.ranking.repository;

import glue.Gachi_Sanchaek.ranking.dto.RankingWithProfileProjection;
import glue.Gachi_Sanchaek.ranking.entity.Ranking;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    @Query(value = """
        SELECT 
            u.nickname AS nickname,
            u.profile_image_url AS profileImageUrl,
            RANK() OVER (ORDER BY r.point DESC) AS ranking
        FROM rankings r
        JOIN user u ON u.id = r.user_id
        WHERE r.rank_period = :period
        ORDER BY r.point DESC
        LIMIT 10
        """, nativeQuery = true)
    List<RankingWithProfileProjection> findTop10ByPeriod(@Param("period") int period);
}
