package glue.Gachi_Sanchaek;

import glue.Gachi_Sanchaek.stamp.config.BonggongProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BonggongProperties.class)
public class GachiSanchaekApplication {

	public static void main(String[] args) {

        SpringApplication.run(GachiSanchaekApplication.class, args);
	}

}
