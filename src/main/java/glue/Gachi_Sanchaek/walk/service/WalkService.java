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
        return rewardService.finalizeWalk(userId,walk,"산책 종료 완료",
                request.getTotalDistance(),request.getTotalMinutes());
    }


    public Object handleQrScan(Long userId, QrVerificationRequest request) {
        return verificationService.handleQrScan(userId,request);
    }

    public WalkEndResponse verifyPlogging(
            Long userId,
            Long walkId,
            MultipartFile image,
            Double totalDistance,
            Integer totalMinutes
    )
    {
        return verificationService.verifyPlogging(userId,walkId,image,totalDistance,totalMinutes);
    }

    public void connectWalk(Long walkId){
        walkRecordService.onWebSocketConnect(walkId);
    }
}
