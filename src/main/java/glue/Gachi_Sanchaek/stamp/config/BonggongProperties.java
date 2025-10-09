package glue.Gachi_Sanchaek.stamp.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "bonggong-list")
public class BonggongProperties {

    private List<Bongogong> bonggongs;

    @Getter @Setter
    public static class Bongogong {
        private String name;
        private String filename;
    }
}
