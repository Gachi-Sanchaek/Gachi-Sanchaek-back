package glue.Gachi_Sanchaek.walk.controller;

import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.walk.service.WalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/walk")
@RequiredArgsConstructor

public class WalkController {
    private final WalkService walkService;
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WalkResponse>> startWalk(
            @RequestBody WalkStartRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = Long.parseLong(userDetails.getUsername());
        return walkService.startWalk(req,userId);
    }
}
