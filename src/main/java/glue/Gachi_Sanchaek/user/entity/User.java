package glue.Gachi_Sanchaek.user.entity;


import glue.Gachi_Sanchaek.user.dto.UserJoinDto;
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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl = "/default.png";

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    @Column(nullable = false)
    private String role = "USER";

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints = 0L;

    @Column(name = "walking_count", nullable = false)
    private Long walkingCount = 0L;

    public User(UserJoinDto userJoinDto) {
        this.kakaoId = userJoinDto.getKakaoId();
        this.email = userJoinDto.getEmail();
        this.nickname = userJoinDto.getUsername();
    }

}

