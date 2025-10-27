package glue.Gachi_Sanchaek.walkRecommendation.entity;

import glue.Gachi_Sanchaek.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Entity
public class WalkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walkRecommendation_id")  // null이어도 됨
    private WalkRecommendation walkRecommendation;

    @Column(nullable = false)
    private LocalDateTime startTime;  // 시작 시간

    @Column(nullable = false)
    private LocalDateTime endTime;  // 끝난 시간

    @Column(nullable = false)
    private double totalDistance;  // 총 거리

    @Column(nullable = false)
    private int totalTime;  // 실제 총 소요시간

    @Column(nullable = false)
    private int point;  // 얻은 포인트


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationMethod verificationMethod;  // 봉사 인증방식

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public enum VerificationMethod{
        QR,
        AI_IMAGE,
        NONE
    }
}
