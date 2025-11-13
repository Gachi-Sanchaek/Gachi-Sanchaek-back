package glue.Gachi_Sanchaek.ranking.controller;

import glue.Gachi_Sanchaek.docs.SecureOperation;
import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.service.RankingService;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking API", description = "랭킹 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    @SecureOperation(summary = "기간별 랭킹 Top10 조회", description = "지정된 기간(date)의 포인트 상위 10명에 대한 랭킹 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RankingResponseDto>>> getRankings(
            @RequestParam("date") Integer period) {
        return ApiResponse.ok(rankingService.findTop10ByPeriod(period));
    }

    @SecureOperation(summary = "내 랭킹 정보 조회", description = "요청한 기간(date)에 해당하는 현재 사용자의 랭킹을 조회합니다.")
    @GetMapping("/my-ranking")
    public ResponseEntity<ApiResponse<RankingResponseDto>> getMyRanking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("date") Integer period
    ){
        return ApiResponse.ok(rankingService.findByPeriodAndId(period, userDetails.getUserId()));
    }
}
