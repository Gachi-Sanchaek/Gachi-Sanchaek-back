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
    //status = ONGOING (QR인증 제외)
    public WalkResponse onConnected(Long walkId) {
        WalkRecord walk = getWalkOrThrow(walkId);
        if(walk.getVerificationMethod()!= VerificationMethod.QR
                && walk.getStatus() == WalkStatus.WAITING){
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordRepository.save(walk);
        }
        return WalkResponse.builder()
                .walkId(walk.getId())
                .status(walk.getStatus())
                .walkType(walk.getWalkType())
                .recommendationId(walk.getWalkRecommendationId())
                .verificationMethod(walk.getVerificationMethod())
                .startTime(walk.getStartTime())
                .build();
    }
}
