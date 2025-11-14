package glue.Gachi_Sanchaek.walk.service;

import glue.Gachi_Sanchaek.walk.dto.VerificationResponse;
import glue.Gachi_Sanchaek.walk.dto.VerificationRequest;
import glue.Gachi_Sanchaek.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.walk.enums.QrStage;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class VerificationService {
    private final GeminiWalkService geminiWalkService;
    private final WalkRecordService walkRecordService;

    @Transactional
    public VerificationResponse verifyQr(Long userId, VerificationRequest request) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(request.getWalkId());

        //산책 기록 소유자 확인
        if(!walk.getUser().getId().equals(userId)){
            throw new SecurityException("산책 기록의 소유자가 아닙니다.");
        }

        if (walk.getVerificationMethod() != VerificationMethod.QR)
            throw new IllegalArgumentException("QR 인증 대상이 아닙니다.");

        return switch (walk.getQrStage()){
            
            //이미 인증 완료된 상태
            case VERIFIED -> new VerificationResponse(
                    walk.getId(), false, "이미 QR 인증이 완료되었습니다"
            );
            
            //첫번째 스캔
            case UNVERIFIED -> {
                walk.setQrToken(request.getQrToken());
                walk.setQrStage(QrStage.FIRST_SCANNED);
                walk.setStatus(WalkStatus.ONGOING);
                yield new VerificationResponse(walk.getId(),true,"QR 첫번째 스캔 완료");
            }
            
            //두번째 스캔
            case FIRST_SCANNED -> {
                
                //첫번째 스캔과 QR 코드 불일치
                if(!request.getQrToken().equals(walk.getQrToken())){
                    yield new VerificationResponse(
                            walk.getId(),false,"1회차 스캔과 QR 불일치 (2회차 스캔)"
                    );
                }
                walk.setQrStage(QrStage.VERIFIED);
                yield new VerificationResponse(walk.getId(),true,"QR 인증 성공");
            }
        };
    }

    //플로깅 AI인증
    @Transactional
    public VerificationResponse verifyPlogging(Long userId, Long walkId, MultipartFile image) {
        WalkRecord walk = walkRecordService.getWalkOrThrow(walkId);

        //산책 기록 소유자 확인
        if(!walk.getUser().getId().equals(userId)){
            throw new SecurityException("산책 기록의 소유자가 아닙니다");
        }

        //산책 상태 검증
        if (walk.getVerificationMethod() != VerificationMethod.AI) {
            throw new IllegalArgumentException("플로깅 인증 대상 산책이 아닙니다.");
        }
        if (walk.getStatus() != WalkStatus.ONGOING) {
            throw new IllegalArgumentException("진행 중인 산책만 플로깅 인증이 가능합니다.");
        }

        //이미 인증 성공한 경우
        if(Boolean.TRUE.equals(walk.getPloggingVerified())){
            return new VerificationResponse(
                    walk.getId(),false,"이미 플로깅 인증이 완료되었습니다"
            );
        }
        //이미지 AI 분석
        int trashCount = geminiWalkService.countTrashImage(image);

        //실패한경우
        if (trashCount < 10) {
            return new VerificationResponse(walk.getId(),
                    false, "플로깅 인증하기에 쓰레기가 부족합니다. (" + trashCount + "개 감지)");
        }

        //성공한 경우 - DB에 저장
        walk.setPloggingVerified(true);

        return new VerificationResponse(walk.getId(),
                true, "플로깅 인증에 성공했습니다 (" + trashCount + "개 감지)");
    }
}