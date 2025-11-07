package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import glue.Gachi_Sanchaek.walk.repository.WalkRecordRepository;
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
    public WalkEndResponse endWalk(Long userId, Long walkId) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(walkId);
        return rewardService.finalizeWalk(userId,walk,"산책 종료 완료");
    }


    public Object handleQrScan(Long userId, String qrToken){
        return verificationService.handleQrScan(userId,qrToken);
    }

    public WalkEndResponse verifyPlogging(Long userId, Long walkId, MultipartFile image){
        return verificationService.verifyPlogging(userId,walkId,image);
    }
}
