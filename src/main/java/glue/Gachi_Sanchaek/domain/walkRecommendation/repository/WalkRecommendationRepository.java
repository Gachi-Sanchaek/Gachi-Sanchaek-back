package glue.Gachi_Sanchaek.domain.walkRecommendation.repository;

import glue.Gachi_Sanchaek.domain.walkRecommendation.entity.WalkRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkRecommendationRepository extends JpaRepository<WalkRecommendation,Long> {

}
