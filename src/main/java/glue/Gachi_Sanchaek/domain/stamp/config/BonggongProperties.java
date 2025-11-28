package glue.Gachi_Sanchaek.domain.stamp.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "bonggong-list")
public class BonggongProperties {

    private final List<Bongogong> bonggongs;

    public BonggongProperties(List<Bongogong> bonggongs) {
        this.bonggongs = bonggongs;
    }

    @Getter
    public static class Bongogong {
        private final String name;
        private final String filename;

        public Bongogong(String name, String filename) {
            this.name = name;
            this.filename = filename;
        }
    }
}