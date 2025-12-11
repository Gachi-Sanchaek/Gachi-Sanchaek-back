package glue.Gachi_Sanchaek.domain.ranking.repository;

import glue.Gachi_Sanchaek.domain.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.domain.ranking.entity.Ranking;
import java.util.List;
import java.util.Optional; // 1. Optional import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    @Query(value = """
        SELECT 
            u.nickname, 
            u.profile_image_url AS profileImageUrl, 
            filtered_r.point, 
            RANK() OVER (ORDER BY filtered_r.point DESC) as ranking
        FROM (
            SELECT user_id, point, updated_at
            FROM rankings
            WHERE rank_period = :period
            ORDER BY point DESC
            LIMIT 50  -- 탈퇴한 유저를 고려하여 10개보다 여유 있게 조회 (인덱스 활용)
        ) filtered_r
        JOIN users u ON u.id = filtered_r.user_id
        WHERE u.is_deleted = false
        ORDER BY filtered_r.point DESC, filtered_r.updated_at ASC
        LIMIT 10
        """, nativeQuery = true)
    List<RankingResponseDto> findTop10ByPeriod(@Param("period") int period);


    @Query(value = """
        SELECT 
            sub.nickname, 
            sub.profileImageUrl, 
            sub.point, 
            sub.ranking
        FROM (
            SELECT 
                r.user_id,
                u.nickname,
                u.profile_image_url AS profileImageUrl,
                r.point,
                RANK() OVER (ORDER BY r.point DESC) AS ranking
            FROM rankings r
            JOIN users u ON u.id = r.user_id
            WHERE r.rank_period = :period
                AND u.is_deleted = false
        ) sub
        WHERE sub.user_id = :userId
    """, nativeQuery = true)
    RankingResponseDto findDtoByPeriodAndUserId(@Param("period") int period, @Param("userId") Long userId);


    Optional<Ranking> findByRankPeriodAndUserId(int period, Long userId);
}