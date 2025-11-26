package glue.Gachi_Sanchaek.common.docs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * /swagger-ui/index.html
     */
    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement =  new SecurityRequirement().addList(jwtSchemeName);

        return new OpenAPI()
                .info(new Info()
                        .title("Gachi Sanchaek API 목록")
                        .description("가톨릭대학교 컴퓨터정보공학부 학술제 - GLUE 해커톤 [믿음소망사랑]팀의 \"가치 산책\" 백엔드 API 목록입니다.")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("https://gachi-sanchaek.shop")
                                .description("운영 서버"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발용 로컬 서버")
                ))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                        )
                );
    }
    @Bean
    public GroupedOpenApi groupedOpenApiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
