package glue.Gachi_Sanchaek.walk.entity;

import glue.Gachi_Sanchaek.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class WalkLocation {
    @Id
    @GeneratedValue
    private Long id;
    private Long walkId;
    private Double latitude;
    private Double longitude;
}
