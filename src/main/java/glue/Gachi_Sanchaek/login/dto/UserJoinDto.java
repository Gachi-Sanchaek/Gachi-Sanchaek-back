package glue.Gachi_Sanchaek.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class UserJoinDto {
    private Long kakaoId;
    private String email;
    private String username;

    public UserJoinDto(KakaoUserInfoResponseDto kakaoUserInfoResponseDto) {
        this.kakaoId = kakaoUserInfoResponseDto.getId();
        this.email = kakaoUserInfoResponseDto.getKakaoAccount().getEmail();
        this.username = kakaoUserInfoResponseDto.getProperties().get("nickname");
    }
}
