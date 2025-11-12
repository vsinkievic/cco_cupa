package lt.creditco.cupa.config;

import com.bpmid.vapp.config.ApiSecurityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;

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

    private final CupaApiBusinessLogicService cupaApiBusinessLogicService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    
    public CupaApiSecurityConfiguration(CupaApiBusinessLogicService cupaApiBusinessLogicService, @Lazy AuthenticationEntryPoint authenticationEntryPoint) {
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
     * @param mvc the MvcRequestMatcher.Builder for path matching (unused but required by signature)
     * @throws Exception if configuration fails
     */
    @Override
    protected void configureApiAuthentication(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        // Replace JWT with X-API-Key authentication filter
        http.addFilterAfter(new ApiKeyAuthenticationFilter(cupaApiBusinessLogicService, authenticationEntryPoint), BasicAuthenticationFilter.class);
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

        // First, apply parent rules (for endpoints that DO exist)
        //super.configureApiAuthorization(http, mvc);  -- we will override all the configuration

        // Then, add our custom rules for /api/** paths
        http.authorizeHttpRequests(authz ->
            authz
                // Merchant API v1 - authenticated by ApiKeyAuthenticationFilter
                .requestMatchers(mvc.pattern("/api/**")).authenticated()
                // Public webhook for external integrations
                .requestMatchers(mvc.pattern("/public/webhook")).permitAll()
        );
    }
}

