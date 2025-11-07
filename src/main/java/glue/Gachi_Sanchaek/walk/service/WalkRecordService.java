package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import glue.Gachi_Sanchaek.walk.repository.WalkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalkRecordService {
    private final WalkRecordRepository walkRecordRepository;
    private final UserService userService;

    public WalkResponse startWalk(WalkStartRequest request, Long userId) {
        User currentUser = userService.findById(userId);

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
    public WalkRecord getWalkOrThrow(Long walkId){
        return walkRecordRepository.findById(walkId)
                .orElseThrow(()-> new IllegalArgumentException("해당 산책 세션이 존재하지 않습니다"));
    }
    //웹소켓 연결 시 status = ONGOING (QR인증 제외)
    public void onWebSocketConnect(Long walkId) {
        WalkRecord walk = getWalkOrThrow(walkId);
        if(walk.getVerificationMethod()!= VerificationMethod.QR
                && walk.getStatus() == WalkStatus.WAITING){
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordRepository.save(walk);
        }
    }
    public WalkRecord findLatestQrWalk(Long userId){
        return walkRecordRepository
                .findTopByUser_IdAndVerificationMethodOrderByStartTimeDesc(userId,VerificationMethod.QR)
                .orElseThrow(()-> new IllegalArgumentException("QR 인증 대상 산책이 존재하지 않습니다"));
    }
}
