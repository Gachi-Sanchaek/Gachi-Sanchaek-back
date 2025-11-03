package glue.Gachi_Sanchaek.walk.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkEndRequest;
import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.service.GeminiWalkService;
import glue.Gachi_Sanchaek.walk.service.WalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/walk")
@RequiredArgsConstructor

public class WalkController {
    private final WalkService walkService;
    private final GeminiWalkService geminiWalkService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WalkResponse>> startWalk(
            @RequestBody WalkStartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkResponse response = walkService.startWalk(request, userId);
        return ApiResponse.ok(response,"산책 시작");
    }
    @PatchMapping("/end")
    public ResponseEntity<ApiResponse<WalkEndResponse>> endWalk(
            @RequestBody WalkEndRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkEndResponse response = walkService.endWalk(userId, request.getWalkId());
        return ApiResponse.ok(response,"산책 종료 성공");
    }

    @PostMapping("/plogging")
    public ResponseEntity<ApiResponse<?>> pLogging(
            @RequestParam("image") MultipartFile image,
            @RequestParam("walkId") Long walkId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        int trashCount = geminiWalkService.countTrashImage(image);

        if(trashCount >= 10){
            WalkEndResponse response = walkService.endWalk(userId, walkId);
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.ok(response, "플로깅 검증 성공");
        }
        else{
            String message = "플로깅 검증 실패. 10개 이상의 쓰레기가 필요합니다";
            return (ResponseEntity<ApiResponse<?>>) (ResponseEntity<?>) ApiResponse.badRequest(message);
        }
    }

    @PostMapping("/qr")
    public ResponseEntity<ApiResponse<WalkResponse>> veriftQr(
            @RequestParam("qrToken") String qrToken,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkResponse response = walkService.handleQrScan(userId,qrToken);
        return ApiResponse.ok(response,"QR 인증 처리 완료");
    }

}
