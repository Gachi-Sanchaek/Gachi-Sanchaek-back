package glue.Gachi_Sanchaek.walk.entity;

import glue.Gachi_Sanchaek.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.walk.enums.QrStage;
import glue.Gachi_Sanchaek.walk.enums.VerificationMethod;
import glue.Gachi_Sanchaek.walk.enums.WalkStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="WalkRecord")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WalkRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name= "walk_recommendation_id")
    private Long walkRecommendationId;

    @Column(name="walk_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private WalkType walkType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name="verification_method",nullable = false)
    private VerificationMethod verificationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private WalkStatus status;

    @Column(name="total_distance")
    private Double totalDistance;

    @Column(name="total_time")
    private Integer totalTime;

    @Column(name="point")
    private Integer point;

    @CreationTimestamp
    @Column(name="created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name= "qr_token")
    private String qrToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="qr_stage")
    private QrStage qrStage = QrStage.UNVERIFIED;

    @Column(name = "plogging_verified")
    private Boolean ploggingVerified = false;

}
