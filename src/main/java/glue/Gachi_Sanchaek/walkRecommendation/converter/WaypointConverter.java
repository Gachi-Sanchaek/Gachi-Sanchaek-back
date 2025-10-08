package glue.Gachi_Sanchaek.walkRecommendation.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import glue.Gachi_Sanchaek.walkRecommendation.entity.Waypoint;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class WaypointConverter implements AttributeConverter<List<Waypoint>,String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Waypoint> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 변환 중 오류 발생", e);
        }
    }

    @Override
    public List<Waypoint> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<List<Waypoint>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("엔티티 변환 중 오류 발생", e);
        }
    }
}
