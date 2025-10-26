package glue.Gachi_Sanchaek.ranking.dto;

public interface RankingWithProfileProjection {

    Long getUserId();
    String getProfileImageUrl();
    int getRanking(); // 순위
}