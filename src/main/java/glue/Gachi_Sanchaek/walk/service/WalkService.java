package glue.Gachi_Sanchaek.walk.service;

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

    public WalkResponse startWalk(WalkStartRequest request) {
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
}
