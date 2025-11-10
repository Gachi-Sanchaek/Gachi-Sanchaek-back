package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.QrVerificationRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final GeminiWalkService geminiWalkService;
    private final RewardService rewardService;
    private final WalkRecordService walkRecordService;

    //QR인식
    public Object handleQrScan(Long userId, QrVerificationRequest request){
        WalkRecord walk = walkRecordService.findLatestQrWalk(userId);

        //처음 스캔할 때
        if(walk.getStatus()== WalkStatus.WAITING){
            walk.setQrToken(request.getQrToken());
            walk.setStatus(WalkStatus.ONGOING);

            return WalkResponse.builder()
                    .walkId(walk.getId())
                    .status(WalkStatus.ONGOING)
                    .walkType(walk.getWalkType())
                    .verificationMethod(walk.getVerificationMethod())
                    .startTime(walk.getStartTime())
                    .build();
        }
        else if(walk.getStatus()==WalkStatus.ONGOING
                && request.getQrToken().equals(walk.getQrToken())){
            return rewardService.finalizeWalk
                    (userId,walk,"QR 인증 성공, 산책 종료 완료",
                            request.getTotalDistance(),
                            request.getTotalMinutes());
        }
        else{
            throw new IllegalArgumentException("잘못된 QR 인증 시도입니다");
        }
    }

    //플로깅 AI인증
    public WalkEndResponse verifyPlogging(Long userId, Long walkId, MultipartFile image
                                            ,Double totalDistance, Integer totalMinutes)
    {
        WalkRecord walk = walkRecordService.getWalkOrThrow(walkId);

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
                    .pointsEarned(0L)
                    .message("플로깅 인증 실패 - 쓰레기 개수 부족 ("+trashCount+"개)")
                    .build();
        }

        //인증 성공 시
        return rewardService.finalizeWalk(
                userId, walk,
                "플로깅 인증 성공 (" + trashCount + "개 감지됨), 산책 종료 완료",
                totalDistance, totalMinutes
        );
    }
}
