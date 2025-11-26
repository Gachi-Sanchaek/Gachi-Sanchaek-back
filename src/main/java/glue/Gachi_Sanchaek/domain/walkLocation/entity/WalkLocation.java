package glue.Gachi_Sanchaek.domain.walkLocation.entity;

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
