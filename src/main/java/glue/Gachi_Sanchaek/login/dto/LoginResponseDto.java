package glue.Gachi_Sanchaek.login.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import glue.Gachi_Sanchaek.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponseDto {

    private boolean isNewUser;
    private String nickname;

    @JsonIgnore
    private User user;

    public LoginResponseDto(boolean isNewUser, User user) {
        this.isNewUser = isNewUser;
        this.nickname = user.getNickname();
        this.user = user;
    }

    @JsonProperty("isNewUser")
    public boolean isNewUser() {
        return isNewUser;
    }
}
