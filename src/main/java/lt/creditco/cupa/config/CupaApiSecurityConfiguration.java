package lt.creditco.cupa.config;

import com.bpmid.vapp.config.ApiSecurityConfiguration;
import com.bpmid.vapp.security.SecurityProblemSupport;

import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.web.filter.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tech.jhipster.config.JHipsterProperties;

/**
 * CUPA API security configuration extending vapp-base's ApiSecurityConfiguration.
 * Replaces JWT authentication with X-API-Key based authentication for merchant API access.
 * 
 * Security configuration:
 * - /api/v1/** - Protected by X-API-Key (handled by ApiKeyAuthenticationFilter)
 * - /api/** - Future API versions, also protected by X-API-Key
 * 
 * Note: Public resources like /public/webhook are configured in CupaVaadinSecurityConfiguration
 * since they are not under /api/** and require the Vaadin filter chain (Order 2).
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class CupaApiSecurityConfiguration extends ApiSecurityConfiguration {

    private final CupaApiBusinessLogicService cupaApiBusinessLogicService;
    private final SecurityProblemSupport problemSupport;
    
    public CupaApiSecurityConfiguration(
            CupaApiBusinessLogicService cupaApiBusinessLogicService,
            SecurityProblemSupport problemSupport,
            JHipsterProperties jHipsterProperties
    ) {
        super(jHipsterProperties, problemSupport);
        this.cupaApiBusinessLogicService = cupaApiBusinessLogicService;
        this.problemSupport = problemSupport;
    }

    /**
     * Configure X-API-Key authentication instead of JWT.
     * Adds ApiKeyAuthenticationFilter to validate X-API-Key header against merchant credentials.
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if configuration fails
     */
    @Override
    protected void configureApiAuthentication(HttpSecurity http) throws Exception {
        // Configure standard exception handling
        http.exceptionHandling(exceptions ->
            exceptions
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        );

        // Replace JWT with X-API-Key authentication filter
        http.addFilterAfter(new ApiKeyAuthenticationFilter(cupaApiBusinessLogicService, problemSupport), BasicAuthenticationFilter.class);
    }

    /**
     * Configure authorization rules for CUPA API endpoints.
     * All /api/** endpoints require authentication (handled by ApiKeyAuthenticationFilter).
     * 
     * Note: This filter chain only handles /api/** paths. Public resources like /public/webhook
     * are configured in CupaVaadinSecurityConfiguration (Vaadin filter chain, Order 2).
     *
     * @param http the HttpSecurity to configure
     * @throws Exception if configuration fails
     */
    @Override
    protected void configureApiAuthorization(HttpSecurity http) throws Exception {
        // Configure authorization for /api/** paths only
        // This filter chain (.securityMatcher("/api/**") in ApiSecurityConfiguration) 
        // only processes requests matching /api/**, so other paths won't reach here

        // super.configureApiAuthorization(http);  commented out to override the default configuration
        http.authorizeHttpRequests(authz ->
            authz
                // All /api/** endpoints require authentication via ApiKeyAuthenticationFilter
                .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .requestMatchers("/api/**").hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.MERCHANT)
        );
    }
}
