package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.VerificationResponse;
import glue.Gachi_Sanchaek.walk.dto.VerificationRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final GeminiWalkService geminiWalkService;
    private final RewardService rewardService;
    private final WalkRecordService walkRecordService;

    // QR 스캔 횟수를 walkId 기준으로 임시 저장
    private final Map<Long, Integer> qrScanCountMap = new ConcurrentHashMap<>();

    public VerificationResponse verifyQr(Long userId, VerificationRequest request) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(request.getWalkId());

        if (walk.getVerificationMethod() != VerificationMethod.QR)
            throw new IllegalArgumentException("QR 인증 대상이 아닙니다.");

        // 1. QR 스캔 횟수 계산
        int newCount = qrScanCountMap.getOrDefault(walk.getId(), 0) + 1;
        qrScanCountMap.put(walk.getId(), newCount);

        // 2. 홀짝 판별
        if (newCount % 2 == 1) {
            // 홀수 → 첫 스캔
            walk.setQrToken(request.getQrToken());
            walk.setStatus(WalkStatus.ONGOING);
            walkRecordService.updateStatusAndToken(walk.getId(), WalkStatus.ONGOING, request.getQrToken());
            return new VerificationResponse(walk.getId(), true, "QR 첫 스캔 완료 (1회차)");
        } else {
            // 짝수 → 두 번째 스캔
            if (request.getQrToken().equals(walk.getQrToken())) {
                return new VerificationResponse(walk.getId(), true, "QR 인증 성공 (2회차)");
            } else {
                return new VerificationResponse(walk.getId(), false, "QR 토큰 불일치 (2회차)");
            }
        }
    }

    //플로깅 AI인증
    public VerificationResponse verifyPlogging(Long userId, Long walkId, MultipartFile image
            , Double totalDistance, Integer totalMinutes) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(walkId);

        //산책 상태 검증
        if (walk.getVerificationMethod() != VerificationMethod.AI) {
            throw new IllegalArgumentException("플로깅 인증 대상 산책이 아닙니다.");
        }
        if (walk.getStatus() != WalkStatus.ONGOING) {
            throw new IllegalArgumentException("진행 중인 산책만 플로깅 인증이 가능합니다.");
        }
        //이미지 AI 분석
        int trashCount = geminiWalkService.countTrashImage(image);

        if (trashCount < 10)
            return new VerificationResponse(walk.getId(),
                    false, "플로깅 인증 실패 (" + trashCount + "개 감지됨)");

        return new VerificationResponse(walk.getId(),
                true, "플로깅 인증 성공 (" + trashCount + "개 감지됨)");
    }
}