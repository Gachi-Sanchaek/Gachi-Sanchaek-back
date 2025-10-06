package glue.Gachi_Sanchaek.user.dto;

import glue.Gachi_Sanchaek.user.entity.User;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    private String profileImageUrl;
    private String nickname;
    private String email;
    private Long kakaoId;
    private LocalDateTime createdAt;
    private Long totalPoints;
    private Long walkingCount;
    private String role;

    public UserResponseDto(User user) {
        this.profileImageUrl = user.getProfileImageUrl();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.kakaoId = user.getKakaoId();
        this.createdAt = user.getCreatedAt();
        this.totalPoints = user.getTotalPoints();
        this.walkingCount = user.getWalkingCount();
        this.role = user.getRole();
    }
}
