package lt.creditco.cupa.config;

import com.bpmid.vapp.config.VaadinSecurityConfiguration;

import tech.jhipster.config.JHipsterProperties;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * CUPA-specific Vaadin security configuration.
 * Extends vapp-base VaadinSecurityConfiguration to add public webhook endpoint.
 */
@Configuration
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
}

