package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.*;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class WalkService {
    private final WalkRecordService walkRecordService;
    private final RewardService rewardService;
    private final VerificationService verificationService;

    //산책 세션 생성 메서드
    public WalkResponse startWalk(WalkStartRequest request, Long userId) {
        return walkRecordService.startWalk(request, userId);
    }

    //일반 산책 시 산책 종료 메서드
    public WalkEndResponse endWalk(Long userId, WalkEndRequest request) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(request.getWalkId());
        return rewardService.finalizeWalk(userId, walk, "산책 종료 완료",
                request.getTotalDistance(), request.getTotalMinutes());
    }

    public VerificationResponse verifyQr(Long userId, VerificationRequest request) {
        return verificationService.verifyQr(userId, request);
    }

    public VerificationResponse verifyPlogging(
            Long userId,
            Long walkId,
            MultipartFile image,
            Double totalDistance,
            Integer totalMinutes
    ) {
        return verificationService.verifyPlogging(userId, walkId, image, totalDistance, totalMinutes);
    }

    public WalkResponse connectWalk(Long walkId) {
        return walkRecordService.onConnected(walkId);
    }
}
    /*
    public WalkEndResponse endWalk(Long userId, WalkEndRequest request) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(request.getWalkId());

        // QR 또는 AI 중 하나라도 인증 성공 상태일 때만 종료 허용
        if (walk.getVerificationMethod() == VerificationMethod.QR && walk.getQrToken() == null) {
            throw new IllegalStateException("QR 인증이 완료되지 않았습니다.");
        }

        // (AI의 경우 gemini 검증은 바로 완료되므로 상태만 확인)
        walk.setStatus(WalkStatus.FINISHED);
        walkRecordService.updateStatusAndToken(walk.getId(), WalkStatus.FINISHED, walk.getQrToken());

        return rewardService.finalizeWalk(
                userId,
                walk,
                "산책 종료 완료",
                request.getTotalDistance(),
                request.getTotalMinutes()
        );
    }
     */
