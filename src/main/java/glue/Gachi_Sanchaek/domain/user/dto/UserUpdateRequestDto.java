package glue.Gachi_Sanchaek.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {
    private String nickname;
    private String profileImageUrl;
}
