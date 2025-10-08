package glue.Gachi_Sanchaek.walkRecommendation.entity;

import glue.Gachi_Sanchaek.organization.entity.Organization;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.walkRecommendation.converter.WaypointConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class WalkRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private int plannedMinutes;  // 사용자가 설정한 산책 시간

    @Column(nullable = false)
    private int actualMinutes; // 실제 걸린 산책 시간(분)

    @Convert(converter = WaypointConverter.class)
    @Column(nullable = false, columnDefinition = "json")
    private List<Waypoint> wayPoints;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
