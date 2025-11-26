package glue.Gachi_Sanchaek.domain.walk.controller;

import glue.Gachi_Sanchaek.common.docs.SecureOperation;
import glue.Gachi_Sanchaek.common.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.domain.walk.dto.VerificationRequest;
import glue.Gachi_Sanchaek.domain.walk.dto.VerificationResponse;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkEndRequest;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkEndResponse;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.common.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.*;
import glue.Gachi_Sanchaek.domain.walk.service.WalkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "산책 진행", description = "산책 진행과 산책 인증 수행 API")
@RestController
@RequestMapping("api/v1/walk")
@RequiredArgsConstructor


public class WalkController {
    private final WalkService walkService;

    @SecureOperation(
            summary = "산책 시작",
            description = "새로운 산책 세션(walkId)을 생성합니다. 산책 상태 status를 WAITING으로 설정합니다"
    )
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WalkResponse>> startWalk(
            @RequestBody WalkStartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkResponse response = walkService.startWalk(request, userId);
        return ApiResponse.ok(response,"산책 시작");
    }

    @SecureOperation(
            summary = "산책 연결",
            description = "산책 상태 status를 ONGOING으로 변경합니다"
    )
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<WalkResponse>> connectWalk(
            @RequestParam Long walkId
    ){
        WalkResponse response = walkService.connectWalk(walkId);
        return ApiResponse.ok(response,"산책 세션 연결 성공");
    }

    @SecureOperation(
            summary="산책 종료",
            description = "산책을 종료합니다. 산책 상태 status를 FINISHED으로 변경합니다"
            +"유저 정보 (유저 닉네임, 유저의 총 산책 횟수)와 최종기록(거리,시간,포인트)를 업데이트합니다 "
    )
    @PatchMapping("/end")
    public ResponseEntity<ApiResponse<WalkEndResponse>> endWalk(
            @RequestBody WalkEndRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkEndResponse response = walkService.endWalk(userId, request);
        return ApiResponse.ok(response,"산책 종료 성공");
    }

    @SecureOperation(
            summary = "플로깅 인증",
            description = "플로깅 활동을 인증하기 위해 쓰레기 사진을 촬영하면 AI가 사진을 분석합니다"
    )
    @PostMapping("/plogging")
    public ResponseEntity<ApiResponse<VerificationResponse>> pLogging(
            @RequestParam("image") MultipartFile image,
            @RequestParam("walkId") Long walkId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());
        VerificationResponse response = walkService.verifyPlogging(userId, walkId, image);
        return ApiResponse.ok(response,response.getMessage());
    }

    @SecureOperation(
            summary = "QR 코드 인증",
            description = "기관 산책 시작과 종료를 인증하기 위해 QR 코드를 스캔합니다"
    )
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
