package glue.Gachi_Sanchaek.ranking.controller;

import glue.Gachi_Sanchaek.ranking.dto.RankingResponseDto;
import glue.Gachi_Sanchaek.ranking.service.RankingService;
import glue.Gachi_Sanchaek.security.jwt.CustomUserDetails;
import glue.Gachi_Sanchaek.util.ApiResponse;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<RankingResponseDto>>> getRankings(
            @RequestParam("date") String date) {
        return ApiResponse.ok(rankingService.findTop10ByPeriod(Integer.parseInt(date)));
    }

    @GetMapping("/my-ranking")
    public ResponseEntity<ApiResponse<RankingResponseDto>> getMyRanking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("date") String date
    ){
        Long id = Long.valueOf(userDetails.getUsername());
        return ApiResponse.ok(rankingService.findByPeriodAndId(Integer.parseInt(date), id));
    }
}
