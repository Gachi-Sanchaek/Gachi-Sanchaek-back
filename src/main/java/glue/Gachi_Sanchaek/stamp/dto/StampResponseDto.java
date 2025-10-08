package glue.Gachi_Sanchaek.stamp.dto;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
public class StampResponseDto {

    private Long id;
    private String name;
    private String imageUrl;
    private Long price;

    public StampResponseDto(Stamp stamp) {
        this.id = stamp.getId();
        this.name = stamp.getName();
        this.imageUrl = stamp.getImageUrl();
        this.price = stamp.getPrice();
    }

}
