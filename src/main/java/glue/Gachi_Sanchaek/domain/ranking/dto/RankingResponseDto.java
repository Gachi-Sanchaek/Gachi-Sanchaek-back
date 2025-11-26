package glue.Gachi_Sanchaek.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RankingResponseDto {
    private String nickname;
    private String profileImageUrl;
    private Long point;
    private Long ranking;
}
