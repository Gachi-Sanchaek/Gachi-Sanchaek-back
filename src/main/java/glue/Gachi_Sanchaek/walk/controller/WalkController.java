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
    @PatchMapping("/end")
    public ResponseEntity<ApiResponse<WalkEndResponse>> endWalk(
            @RequestBody WalkEndRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        WalkEndResponse response = walkService.endWalk(userId, request.getWalkId());
        return ApiResponse.ok(response,"산책 종료 성공");
    }

}
