package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import glue.Gachi_Sanchaek.walk.repository.WalkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalkService {
    private final WalkRecordRepository walkRecordRepository;
    private final UserRepository userRepository;

    public ResponseEntity<ApiResponse<WalkResponse>> startWalk(WalkStartRequest request, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found: "+userId));


        VerificationMethod verificationMethod = switch(request.getWalkType().toUpperCase()){
            case "PLOGGING" -> VerificationMethod.AI;
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

        WalkResponse responseDto = WalkResponse.builder()
                        .walkId(walkRecord.getId())
                        .status(walkRecord.getStatus())
                        .walkType(walkRecord.getWalkType())
                        .recommendationId(walkRecord.getWalkRecommendationId())
                        .verificationMethod(walkRecord.getVerificationMethod())
                        .startTime(walkRecord.getStartTime())
                        .build();
        return ApiResponse.ok(responseDto,"산책 시작");
    }
}
