package glue.Gachi_Sanchaek.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
