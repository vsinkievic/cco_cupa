package lt.creditco.cupa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
// @Profile("prod")
public class OpenAPIConfiguration {

    @Value("${jhipster.mail.base-url:}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(baseUrl);
        server.setDescription("Production server");

        return new OpenAPI().servers(List.of(server))
            .components(new Components()
                    .addSecuritySchemes("X-API-Key", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                    )
                )
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"));
    }

}
