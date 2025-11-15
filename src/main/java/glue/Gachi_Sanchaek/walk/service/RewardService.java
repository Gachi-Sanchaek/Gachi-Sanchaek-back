package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.organization.entity.UserOrganization;
import glue.Gachi_Sanchaek.organization.service.OrganizationService;
import glue.Gachi_Sanchaek.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.ranking.service.RankingService;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import glue.Gachi_Sanchaek.walk.repository.WalkRecordRepository;
import glue.Gachi_Sanchaek.walkRecommendation.repository.WalkRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final WalkRecordRepository walkRecordRepository;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final PointLogService pointLogService;
    private final RankingService rankingService;

    //산책 종료 공통 로직
    public WalkEndResponse finalizeWalk(Long userId, WalkRecord walk, String message,
                                        Double totalDistance, Integer totalSeconds){

        Long reward = Long.valueOf(calculateReward(walk,totalDistance));

        //인증 성공 시 - WalkRecord 업데이트
        walk.setStatus(WalkStatus.FINISHED);
        walk.setEndTime(LocalDateTime.now());
        walk.setTotalDistance(totalDistance);
        walk.setTotalTime(totalSeconds);
        walkRecordRepository.save(walk);

        User user = userService.findById(userId);
        Long walkingCount = user.getWalkingCount();

        processAfterWalk(userId,walk,reward);

        return WalkEndResponse.builder()
                .walkId(walk.getId())
                .status(WalkStatus.FINISHED)
                .nickname(user.getNickname())
                .totalDistance(totalDistance)
                .totalTime(formatSeconds(totalSeconds))
                .pointsEarned(reward)
                .walkingCount(walkingCount)
                .message(message)
                .build();
    }


    //리워드 랭킹 후처리 메서드
    private void processAfterWalk(Long userId, WalkRecord walk, long reward){
        //산책 타입
        String type = walk.getWalkType();
        String upperWalkType = type.toUpperCase();

        //복지관,보호소 이름 가져오기
        String locationName = "";
        if(upperWalkType.equals("SENIOR") || upperWalkType.equals("DOG")){
            locationName = organizationService.getLocationName(userId);
        }

        //후처리 호출 : 포인트 및 순위 갱신
        userService.recordWalkingResult(userId,reward);
        pointLogService.save(walk.getUser(),reward,type,locationName);
        rankingService.updateRanking(userId,reward);
    }

    // 리워드 계산
    private int calculateReward(WalkRecord walk, double distanceKm){
        //거리 기반 기본 포인트 : 1km당 100점 (반올림)
        int basePoints = (int)Math.round(distanceKm*100);

        //산책 타입에 따른 추가 포인트
        int bonusPoints = switch(walk.getWalkType().toUpperCase()){
            case "SENIOR" -> 400;
            case "DOG" -> 300;
            case "PLOGGING" -> {
                if(Boolean.TRUE.equals(walk.getPloggingVerified())){
                    yield 200;
                }
                else{
                    yield 0;
                }
            }
            default -> 0;
        };
        return basePoints + bonusPoints;
    }

    //산책 시간 계산
    private String formatSeconds(Integer totalSeconds){
        int minutes = totalSeconds/60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
