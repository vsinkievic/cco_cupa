package lt.creditco.cupa.config;

import com.bpmid.vapp.config.VaadinSecurityConfiguration;

import tech.jhipster.config.JHipsterProperties;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * CUPA-specific Vaadin security configuration.
 * Extends vapp-base VaadinSecurityConfiguration to add public webhook endpoint.
 * Marked as @Primary to be used when VaadinSecurityConfiguration is injected.
 */
@Configuration
@Primary
public class CupaVaadinSecurityConfiguration extends VaadinSecurityConfiguration {

    public CupaVaadinSecurityConfiguration(Environment env, JHipsterProperties jHipsterProperties) {
        super(env, jHipsterProperties);
    }

    /**
     * Configure public resources specific to CUPA.
     * Adds /public/webhook endpoint for external payment gateway callbacks.
     * This is called BEFORE authentication rules, ensuring webhooks are publicly accessible.
     *
     * @param authz the authorization registry to configure
     */
    @Override
    protected void configurePublicResources(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        // Public webhook for payment gateway callbacks (no authentication required)
        super.configurePublicResources(authz);  // you can comment out this if you want to override the default public resources
        authz.requestMatchers(pathMatchers("/public/webhook")).permitAll();
    }

    /**
     * CUPA-specific API documentation access control.
     * Extends vapp-base roles (ADMIN, APIDOCS) by adding MERCHANT role.
     */
    @Override
    public String[] getAllowedApiDocsRoles() {
        // Get base roles from vapp-base (ADMIN, APIDOCS)
        String[] baseRoles = super.getAllowedApiDocsRoles();
        
        // Combine with CUPA-specific roles
        return Stream.concat(
            Arrays.stream(baseRoles),
            Stream.of(lt.creditco.cupa.security.AuthoritiesConstants.MERCHANT)
        ).toArray(String[]::new);
    }
}

