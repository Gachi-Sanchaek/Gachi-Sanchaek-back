package glue.Gachi_Sanchaek.userStamp.dto;

import glue.Gachi_Sanchaek.userStamp.entity.UserStamp;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStampResponseDto {

    private Long stampId;
    private LocalDateTime createdAt;

    public UserStampResponseDto(UserStamp userStamp) {
        stampId = userStamp.getStamp().getId();
        createdAt = userStamp.getCreatedAt();
    }

}
