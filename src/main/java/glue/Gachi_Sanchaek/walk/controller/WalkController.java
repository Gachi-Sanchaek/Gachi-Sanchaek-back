package glue.Gachi_Sanchaek.walk.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.*;
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
    public ResponseEntity<ApiResponse<WalkResponse>> connectWalk(
            @RequestParam Long walkId
    ){
        WalkResponse response = walkService.connectWalk(walkId);
        return ApiResponse.ok(response,"산책 세션 연결 성공");
    }
    @PatchMapping("/end")
    public ResponseEntity<ApiResponse<WalkEndResponse>> endWalk(
            @RequestBody WalkEndRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkEndResponse response = walkService.endWalk(userId, request);
        return ApiResponse.ok(response,"산책 종료 성공");
    }

    @PostMapping("/plogging")
    public ResponseEntity<ApiResponse<VerificationResponse>> pLogging(
            @RequestParam("image") MultipartFile image,
            @RequestParam("walkId") Long walkId,
            @RequestParam("totalDistance") Double totalDistance,
            @RequestParam("totalMinutes") Integer totalMinutes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        VerificationResponse response = walkService.verifyPlogging(
                userId, walkId, image,totalDistance,totalMinutes);
        return ApiResponse.ok(response,response.getMessage());
    }

    @PostMapping("/qr")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyQr(
            @RequestBody VerificationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        VerificationResponse response = walkService.verifyQr(userId,request);
        return ApiResponse.ok(response,response.getMessage());
    }

}
