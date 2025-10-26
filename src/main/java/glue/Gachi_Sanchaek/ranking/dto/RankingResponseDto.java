package glue.Gachi_Sanchaek.ranking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankingResponseDto {
    private String nickname;
    private String profileImageUrl;
    private Long point;
    private int ranking;
}
