package glue.Gachi_Sanchaek.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NicknameValidateResponseDto {

    private String nickname;
    private boolean isAvailable;

    public NicknameValidateResponseDto(String nickname, boolean isAvailable) {
        this.nickname = nickname;
        this.isAvailable = isAvailable;
    }

    @JsonProperty("isAvailable")
    public boolean isAvailable() {
        return isAvailable;
    }
}
