package lt.creditco.cupa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class OpenAPIConfiguration {

    @Value("${jhipster.mail.base-url:}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(baseUrl);
        server.setDescription("Production server");

        return new OpenAPI().servers(List.of(server));
    }
}
