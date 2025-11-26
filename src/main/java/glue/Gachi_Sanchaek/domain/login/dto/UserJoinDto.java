package glue.Gachi_Sanchaek.domain.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserJoinDto {
    private Long kakaoId;
    private String username;

    public UserJoinDto(KakaoUserInfoResponseDto kakaoUserInfoResponseDto) {
        this.kakaoId = kakaoUserInfoResponseDto.getId();
        this.username = kakaoUserInfoResponseDto.getProperties().get("nickname");
    }

    public UserJoinDto(Long kakaoId, String username) {
        this.kakaoId = kakaoId;
        this.username = username;
    }
}
