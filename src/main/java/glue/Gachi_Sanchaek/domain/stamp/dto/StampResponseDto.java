package glue.Gachi_Sanchaek.domain.stamp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import glue.Gachi_Sanchaek.domain.stamp.entity.Stamp;
import lombok.Getter;

@Getter
public class StampResponseDto {

    private Long id;
    private String name;
    private String imageUrl;
    private Long price;
    private boolean isActive;

    public StampResponseDto(Stamp stamp) {
        this.id = stamp.getId();
        this.name = stamp.getName();
        this.imageUrl = stamp.getImageUrl();
        this.price = stamp.getPrice();
    }

    @JsonProperty("isActive")
    public boolean isActive(){
        return isActive;
    }

    public void checkActivable(Long userPoints){
        this.isActive = userPoints >= this.price;
    }

}
