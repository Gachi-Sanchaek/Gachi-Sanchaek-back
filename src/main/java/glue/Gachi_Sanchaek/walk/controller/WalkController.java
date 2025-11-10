package glue.Gachi_Sanchaek.walk.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkEndRequest;
import glue.Gachi_Sanchaek.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
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

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WalkResponse>> startWalk(
            @RequestBody WalkStartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkResponse response = walkService.startWalk(request, userId);
        return ApiResponse.ok(response,"산책 시작");
    }

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<String>> connectWalk(
            @RequestParam Long walkId
    ){
        walkService.connectWalk(walkId);
        return ApiResponse.ok("산책 세션 연결 성공");
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
    public ResponseEntity<ApiResponse<WalkEndResponse>> pLogging(
            @RequestParam("image") MultipartFile image,
            @RequestParam("walkId") Long walkId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkEndResponse response = walkService.verifyPlogging(userId, walkId, image);
        return ApiResponse.ok(response,response.getMessage());
    }

    @PostMapping("/qr")
    public ResponseEntity<ApiResponse<Object>> verifyQr(
            @RequestParam("qrToken") String qrToken,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        Object response = walkService.handleQrScan(userId,qrToken);
        return ApiResponse.ok(response,"QR 인증 처리 완료");
    }

}
