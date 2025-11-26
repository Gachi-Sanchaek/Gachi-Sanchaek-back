package glue.Gachi_Sanchaek.domain.walkRecommendation.entity;

import glue.Gachi_Sanchaek.domain.organization.entity.Organization;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.walkRecommendation.convert.WaypointConverter;
import glue.Gachi_Sanchaek.domain.walkRecommendation.dto.Waypoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class WalkRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    @Column(name = "recommendation_group_id",nullable = false)
    private String groupId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int plannedMinutes;  // 사용자가 설정한 산책 시간

    @Convert(converter = WaypointConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<Waypoint> wayPoints;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

}
