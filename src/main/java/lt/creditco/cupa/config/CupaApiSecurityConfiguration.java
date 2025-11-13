package lt.creditco.cupa.config;

import com.bpmid.vapp.config.ApiSecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.web.filter.ApiKeyAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
    private final AuthenticationEntryPoint authenticationEntryPoint;
    
    public CupaApiSecurityConfiguration(
            CupaApiBusinessLogicService cupaApiBusinessLogicService,
            @Lazy AuthenticationEntryPoint authenticationEntryPoint,
            tech.jhipster.config.JHipsterProperties jHipsterProperties
    ) {
        super(jHipsterProperties);
        this.cupaApiBusinessLogicService = cupaApiBusinessLogicService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * This bean is responsible for handling authentication failures (e.g., no/bad API key)
     * and returning a 401 Unauthorized response.
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            // Set the response status
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.JSON_UTF_8.toString());

            // Create a custom error body
            Map<String, Object> body = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", authException.getMessage(), // This gets the "Invalid X-API-Key" etc.
                "path", request.getRequestURI(),
                "timestamp", Instant.now().toString()
            );

            // Write the JSON body to the response
            response.getWriter().write(objectMapper.writeValueAsString(body));
        };
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
        // Replace JWT with X-API-Key authentication filter
        http.addFilterAfter(new ApiKeyAuthenticationFilter(cupaApiBusinessLogicService, authenticationEntryPoint), BasicAuthenticationFilter.class);
        // No JWT/OAuth2 configuration needed
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

