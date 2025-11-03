package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.ranking.service.RankingService;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import glue.Gachi_Sanchaek.walk.repository.WalkRecordRepository;
import glue.Gachi_Sanchaek.walkRecommendation.repository.WalkRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalkService {
    private final WalkRecordRepository walkRecordRepository;
    private final UserRepository userRepository;
    private final WalkLocationService walkLocationService;

    private final UserService userService;
    private final PointLogService pointLogService;
    private final RankingService rankingService;
    private final WalkRecommendationRepository walkRecommendationRepository;


    public WalkResponse startWalk(WalkStartRequest request, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found: "+userId));


        VerificationMethod verificationMethod = switch(request.getWalkType().toUpperCase()){
            case "PLOGGING" -> VerificationMethod.AI;
            case "NORMAL" -> VerificationMethod.NONE;
            default -> VerificationMethod.QR;
        };
        WalkRecord walkRecord = WalkRecord.builder()
                .walkRecommendationId(request.getRecommendationId())
                .walkType(request.getWalkType())
                .verificationMethod(verificationMethod)
                .status(WalkStatus.WAITING)
                .startTime(LocalDateTime.now())
                .user(currentUser)
                .build();
        walkRecordRepository.save(walkRecord);

        return WalkResponse.builder()
                        .walkId(walkRecord.getId())
                        .status(walkRecord.getStatus())
                        .walkType(walkRecord.getWalkType())
                        .recommendationId(walkRecord.getWalkRecommendationId())
                        .verificationMethod(walkRecord.getVerificationMethod())
                        .startTime(walkRecord.getStartTime())
                        .build();
    }
    public WalkEndResponse endWalk(Long userId, Long walkId) {
        WalkRecord walk = walkRecordRepository.findById(walkId)
                .orElseThrow(()-> new IllegalArgumentException("해당 산책 세션이 존재하지 않습니다"));

        double distanceKm = walkLocationService.getTotalDistance(walkId);
        long totalMinutes = walkLocationService.getTotalMinutes(walkId);
        Long reward = Long.valueOf(calculateReward(walk.getWalkType(),distanceKm));

        //산책 타입
        String type = walk.getWalkType();

        //복지관,보호소 이름 가져오기
        String locationName = "NORMAL";
        if(walk.getWalkRecommendationId() != null){
            locationName = walkRecommendationRepository.findById(walk.getWalkRecommendationId())
                    .map(rec -> {
                        if(rec.getOrganization()!=null){
                            return rec.getOrganization().getName();
                        }
                        else{
                            return "NORMAL";
                        }
                    })
                    .orElse("NORMAL");
        }
        //WalkRecord 업데이트
        walk.setStatus(WalkStatus.FINISHED);
        walk.setEndTime(LocalDateTime.now());
        walkRecordRepository.save(walk);

        //후처리 호출 : 포인트 및 순위 갱신
        userService.recordWalkingResult(userId,reward);
        //pointLogService.save(walk.getUser(),reward,type,locationName);
        rankingService.updateRanking(userId,reward);

        //DTO로 반환
        return WalkEndResponse.builder()
                .walkId(walkId)
                .totalDistance(distanceKm)
                .totalMin(totalMinutes)
                .pointsEarned(reward)
                .message("산책 종료 완료")
                .build();
    }

    //QR인식
    public WalkResponse handleQrScan(Long userId, String qrToken){
        WalkRecord walk = walkRecordRepository
                .findTopByUser_IdAndVerificationMethodOrderByStartTimeDesc(userId,VerificationMethod.QR)
                .orElseThrow(() -> new IllegalArgumentException("QR 인증 대상 산책 세션이 존재하지 않습니다"));

        //처음 스캔할 때
        if(walk.getQrToken()==null){
            walk.setQrToken(qrToken);
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordRepository.save(walk);

            return WalkResponse.builder()
                    .walkId(walk.getId())
                    .status(walk.getStatus())
                    .walkType(walk.getWalkType())
                    .verificationMethod(walk.getVerificationMethod())
                    .startTime(walk.getStartTime())
                    .build();
        }

        //두번째 스캔 - QR 일치하면 FINISHED 처리
        if(walk.getQrToken().equals(qrToken)){
            walk.setStatus(WalkStatus.FINISHED);
            walk.setEndTime(LocalDateTime.now());
            walkRecordRepository.save(walk);

            return WalkResponse.builder()
                    .walkId(walk.getId())
                    .status(walk.getStatus())
                    .walkType(walk.getWalkType())
                    .verificationMethod(walk.getVerificationMethod())
                    .startTime(walk.getStartTime())
                    .build();
        }
        throw new IllegalArgumentException("산책 전후 QR 코드가 일치하지 않습니다");
    }
    private int calculateReward(String walkType, double distanceKm){
        //거리 기반 기본 포인트 : 1km당 100점 (반올림)
        int basePoints = (int)Math.round(distanceKm*100);

        //산책 타입에 따른 추가 포인트
        int bonusPoints = switch(walkType){
            case "SENIOR" -> 400;
            case "DOG" -> 300;
            case "PLOGGING" -> 200;
            default -> 0;
        };
        return basePoints + bonusPoints;
    }
}
