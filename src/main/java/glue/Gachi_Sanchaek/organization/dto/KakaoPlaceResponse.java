package glue.Gachi_Sanchaek.organization.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoPlaceResponse {

    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)

    public static class Document {

        @JsonProperty("id")
        private String kakaoId;

        @JsonProperty("place_name")
        private String name;

        @JsonProperty("address_name")
        private String address;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;

        @JsonProperty("distance")
        private String distance;
    }
}