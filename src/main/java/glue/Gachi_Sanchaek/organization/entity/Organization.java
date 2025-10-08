package glue.Gachi_Sanchaek.organization.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long kakaoPlaceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationCategory category;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public enum OrganizationCategory{
        SHELTER,  // 유기견 보호소
        WELFARE   // 복지관
    }
}
