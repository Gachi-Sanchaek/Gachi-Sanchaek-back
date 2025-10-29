package glue.Gachi_Sanchaek.user.entity;


import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_image_url", nullable = false)
    @Builder.Default
    private String profileImageUrl = "/bonggong/1_default.png";

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private String gender = "NONE";

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Long totalPoints = 0L;

    @Column(name = "walking_count", nullable = false)
    @Builder.Default
    private Long walkingCount = 0L;

    public User(UserJoinDto userJoinDto) {
        this();
        this.kakaoId = userJoinDto.getKakaoId();
        this.email = userJoinDto.getEmail();
        this.nickname = userJoinDto.getUsername();
    }

    public void applyJoinInfo(String nickname, String gender) {
        this.nickname = nickname;
        this.gender = gender;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void addTotalPoints(Long reward){
        this.totalPoints += reward;
    }

    public void incrementWalkingCount(){
        this.walkingCount += 1L;
    }

}

