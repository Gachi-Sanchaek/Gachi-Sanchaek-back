package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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
    public WalkRecord getWalkOrThrow(Long walkId){
        return walkRecordRepository.findById(walkId)
                .orElseThrow(()-> new IllegalArgumentException("해당 산책 세션이 존재하지 않습니다"));
    }
}
