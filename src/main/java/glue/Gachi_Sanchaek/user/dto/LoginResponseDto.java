package glue.Gachi_Sanchaek.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LoginResponseDto {

    private boolean isNewUser;
    private String nickname;

    public LoginResponseDto(boolean isNewUser, String nickname) {
        this.isNewUser = isNewUser;
        this.nickname = nickname;
    }

    @JsonProperty("isNewUser")
    public boolean isNewUser() {
        return isNewUser;
    }
}
