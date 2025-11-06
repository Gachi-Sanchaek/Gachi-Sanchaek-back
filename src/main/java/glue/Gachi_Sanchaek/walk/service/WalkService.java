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
import org.springframework.web.multipart.MultipartFile;

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
    private final GeminiWalkService geminiWalkService;


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

    //일반 산책 시 산책 종료 메서드
    public WalkEndResponse endWalk(Long userId, Long walkId) {
        WalkRecord walk = getWalkOrThrow(walkId);
        return finalizeWalk(userId,walk,"산책 종료 완료");
    }

    //QR인식
    public Object handleQrScan(Long userId, String qrToken){
        WalkRecord walk = walkRecordRepository
                .findTopByUser_IdAndVerificationMethodOrderByStartTimeDesc(userId,VerificationMethod.QR)
                .orElseThrow(() -> new IllegalArgumentException("QR 인증 대상 산책 세션이 존재하지 않습니다"));

        //처음 스캔할 때
        if(walk.getStatus()==WalkStatus.WAITING){
            walk.setQrToken(qrToken);
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordRepository.save(walk);

            return WalkResponse.builder()
                    .walkId(walk.getId())
                    .status(WalkStatus.ONGOING)
                    .walkType(walk.getWalkType())
                    .verificationMethod(walk.getVerificationMethod())
                    .startTime(walk.getStartTime())
                    .build();
        }
        else if(walk.getStatus()==WalkStatus.ONGOING
                && walk.getQrToken().equals(qrToken)){
            return finalizeWalk(userId,walk,"QR 인증 성공, 산책 종료 완료");
        }
        else{
            throw new IllegalArgumentException("잘못된 QR 인증 시도입니다");
        }
    }

    //플로깅 AI인증
    public WalkEndResponse verifyPlogging(Long userId, Long walkId, MultipartFile image ){
        WalkRecord walk = getWalkOrThrow(walkId);

        //산책 상태 검증
        if(walk.getVerificationMethod()!=VerificationMethod.AI){
            throw new IllegalArgumentException("플로깅 인증 대상 산책이 아닙니다.");
        }
        if(walk.getStatus()!=WalkStatus.ONGOING){
            throw new IllegalArgumentException("진행 중인 산책만 플로깅 인증이 가능합니다.");
        }
        //이미지 AI 분석
        int trashCount = geminiWalkService.countTrashImage(image);

        //인증 실패 시
        if(trashCount<10){
            return WalkEndResponse.builder()
                    .walkId(walkId)
                    .totalDistance(walkLocationService.getTotalDistance(walkId))
                    .totalMin(walkLocationService.getTotalMinutes(walkId))
                    .pointsEarned(0L)
                    .message("플로깅 인증 실패 - 쓰레기 개수 부족 ("+trashCount+"개)")
                    .build();
        }

        //인증 성공 시
        return finalizeWalk(userId, walk, "플로깅 인증 성공 ("+trashCount+"개 감지됨), 산책 종료 완료");
    }

    // 리워드 계산
    private int calculateReward(String walkType, double distanceKm){
        //거리 기반 기본 포인트 : 1km당 100점 (반올림)
        int basePoints = (int)Math.round(distanceKm*100);

        //산책 타입에 따른 추가 포인트
        int bonusPoints = switch(walkType.toUpperCase()){
            case "SENIOR" -> 400;
            case "DOG" -> 300;
            case "PLOGGING" -> 200;
            default -> 0;
        };
        return basePoints + bonusPoints;
    }
    public void onWebSocketConnect(Long walkId) {
        WalkRecord walk = walkRecordRepository.findById(walkId)
                .orElseThrow(()->new IllegalArgumentException("해당 산책 세션이 존재하지 않습니다."));
        if(walk.getVerificationMethod()!= VerificationMethod.QR
                && walk.getStatus() == WalkStatus.WAITING){
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordRepository.save(walk);
        }
    }

    //리워드 랭킹 후처리 메서드
    private void processAfterWalk(Long userId, WalkRecord walk, long reward){
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

        //후처리 호출 : 포인트 및 순위 갱신
        userService.recordWalkingResult(userId,reward);
        pointLogService.save(walk.getUser(),reward,type,locationName);
        rankingService.updateRanking(userId,reward);
    }

    //산책 종료 공통 로직
    private WalkEndResponse finalizeWalk(Long userId, WalkRecord walk, String message){

        double distanceKm = walkLocationService.getTotalDistance(walk.getId());
        long totalMinutes = walkLocationService.getTotalMinutes(walk.getId());
        Long reward = Long.valueOf(calculateReward(walk.getWalkType(),distanceKm));

        //인증 성공 시 - WalkRecord 업데이트
        walk.setStatus(WalkStatus.FINISHED);
        walk.setEndTime(LocalDateTime.now());
        walkRecordRepository.save(walk);

        processAfterWalk(userId,walk,reward);

        return WalkEndResponse.builder()
                .walkId(walk.getId())
                .totalDistance(distanceKm)
                .totalMin(totalMinutes)
                .pointsEarned(reward)
                .message(message)
                .build();
    }
    private WalkRecord getWalkOrThrow(Long walkId){
        return walkRecordRepository.findById(walkId)
                .orElseThrow(()-> new IllegalArgumentException("해당 산책 세션이 존재하지 않습니다"));
    }
}
