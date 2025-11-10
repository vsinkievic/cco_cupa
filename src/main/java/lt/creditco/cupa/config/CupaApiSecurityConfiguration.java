package lt.creditco.cupa.config;

import com.bpmid.vapp.config.ApiSecurityConfiguration;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.web.filter.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

/**
 * CUPA API security configuration extending vapp-base's ApiSecurityConfiguration.
 * Replaces JWT authentication with X-API-Key based authentication for merchant API access.
 * 
 * Security configuration:
 * - /api/v1/** - Protected by X-API-Key (handled by ApiKeyAuthenticationFilter)
 * - /api/** - Future API versions, also protected by X-API-Key
 * - /public/webhook - Public webhook endpoint for external integrations
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class CupaApiSecurityConfiguration extends ApiSecurityConfiguration {

    private final MerchantRepository merchantRepository;

    public CupaApiSecurityConfiguration(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /**
     * Configure X-API-Key authentication instead of JWT.
     * Adds ApiKeyAuthenticationFilter to validate X-API-Key header against merchant credentials.
     *
     * @param http the HttpSecurity to configure
     * @param mvc the MvcRequestMatcher.Builder for path matching (unused but required by signature)
     * @throws Exception if configuration fails
     */
    @Override
    protected void configureApiAuthentication(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        // Replace JWT with X-API-Key authentication filter
        http.addFilterAfter(new ApiKeyAuthenticationFilter(merchantRepository), BasicAuthenticationFilter.class);
        // No JWT/OAuth2 configuration needed
    }

    /**
     * Configure authorization rules for CUPA API endpoints.
     * All API endpoints are public (permitAll) because authentication is handled by ApiKeyAuthenticationFilter.
     *
     * @param http the HttpSecurity to configure
     * @param mvc the MvcRequestMatcher.Builder for path matching
     * @throws Exception if configuration fails
     */
    @Override
    protected void configureApiAuthorization(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http.authorizeHttpRequests(authz ->
            authz
                // Merchant API v1 - authenticated by ApiKeyAuthenticationFilter
                .requestMatchers(mvc.pattern("/api/v1/**")).permitAll()
                // Future API versions - also use X-API-Key
                .requestMatchers(mvc.pattern("/api/**")).permitAll()
                // Public webhook for external integrations
                .requestMatchers(mvc.pattern("/public/webhook")).permitAll()
        );
    }
}

