package glue.Gachi_Sanchaek.walkRecommendation.service;

import glue.Gachi_Sanchaek.walkRecommendation.entity.WalkRecommendation;
import glue.Gachi_Sanchaek.walkRecommendation.repository.WalkRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalkRecommendationService {

    private final WalkRecommendationRepository walkRecommendationRepository;

    //public List<WalkRecommendation> findByGroupId(){
      //  return walkRecommendationRepository.findByGroupId();
    //}

}
