package lt.creditco.cupa.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;

import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for API key authentication.
 * Reads X-API-KEY header and validates it against merchant API keys.
 * If validation fails, continues with normal Vaadin authentication.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
//    private static final String API_PATH = "/api/v1";
    private static final String API_PATH = "/api/";

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final CupaApiBusinessLogicService cupaApiBusinessLogicService;

    public ApiKeyAuthenticationFilter(CupaApiBusinessLogicService cupaApiBusinessLogicService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.cupaApiBusinessLogicService = cupaApiBusinessLogicService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // Only process API v1 requests

        if (LOG.isTraceEnabled()) LOG.trace("doFilterInternal request: {}", request.getRequestURI());
        if (!request.getRequestURI().startsWith(API_PATH)) {
            if (LOG.isTraceEnabled()) LOG.trace("doFilterInternal request: {} is not a {} request, skipping X-API-Key authentication", request.getRequestURI(), API_PATH);

            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(Constants.API_KEY_HEADER);

        try {
            CupaApiContext.CupaApiContextData contextData = CupaApiContext.getContext();
            if (StringUtils.hasText(apiKey)) {
                // Try to authenticate with API key
                if (LOG.isTraceEnabled()) LOG.trace("doFilterInternal request: {} has API key, authenticating with X-API-Key", request.getRequestURI());

                if (contextData == null || !apiKey.equals(contextData.getCupaApiKey())) {
                    contextData = cupaApiBusinessLogicService.extractBusinessContext(request, null, null);
                    CupaApiContext.setContext(contextData);
                }
                if ( LOG.isTraceEnabled() &&contextData != null && contextData.isAuthenticated())
                    LOG.trace("API key authentication successful for request: {}", request.getRequestURI());
            } else {
                if (LOG.isTraceEnabled()) LOG.trace("No X-API-Key, checking Vaadin authentication");
                Authentication vaadinAuth = SecurityContextHolder.getContext().getAuthentication();
                if (vaadinAuth != null && vaadinAuth.isAuthenticated() && !(vaadinAuth instanceof AnonymousAuthenticationToken)) {
                    if (LOG.isTraceEnabled()) LOG.trace("Vaadin authentication successful, creating API-compatible Authentication object");
                    // Create an API-compatible Authentication object from the Vaadin user

                    if (contextData == null || contextData.getUser() == null || contextData.getUser().getLogin().equals(vaadinAuth.getPrincipal())){
                        contextData = cupaApiBusinessLogicService.extractBusinessContext(request, null, toPrincipal(vaadinAuth.getPrincipal()));
                        CupaApiContext.setContext(contextData);
                    }
                    if (contextData != null && contextData.isAuthenticated())
                        LOG.trace("Vaadin authentication successful for request: {}", request.getRequestURI());
                } else {
                    if (LOG.isTraceEnabled()) LOG.trace("Vaadin authentication failed, skipping X-API-Key authentication");
                    throw new BadCredentialsException("Authentication failed");
                }
            }
            if (contextData == null|| contextData.getMerchantContext() == null) {
                throw new AuthenticationServiceException("Unable to determine authentication context");
            }
            if (!contextData.isAuthenticated()) {
                LOG.warn("{}: authentication failed: {}", request.getRequestURI(), contextData.getMerchantContext().getSecurityRemarks());
                throw new BadCredentialsException(contextData.getMerchantContext().getSecurityRemarks());
            }

            createAuthenticationToken(contextData);
            filterChain.doFilter(request, response);

        } catch (AuthenticationException e) {
            // --- FAILURE (This is your answer) ---
            // This catches BadCredentialsException, etc.
            LOG.warn("API key authentication failed for request {}: {}", request.getRequestURI(), e.getMessage());

            // Clear any lingering context
            SecurityContextHolder.clearContext();
            CupaApiContext.clearContext();

            // Use the entry point to commence the 401 response
            this.authenticationEntryPoint.commence(request, response, e);
            
            // --- STOP THE FILTER CHAIN ---
            return;

        } finally {
            // Always clear the ThreadLocal context after the request
        }
    }

    private Principal toPrincipal(Object principal) {
        if (principal instanceof Principal) {
            return (Principal) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.User user) {
            // Create a simple Principal implementation that wraps the username
            // Principal interface requires getName() method, so we map getUsername() to getName()
            return new Principal() {
                @Override
                public String getName() {
                    return user.getUsername();
                }
            };
        } else if (principal instanceof String username) {
            // If principal is already a String (username), create a simple Principal wrapper
            return () -> username;
        }
        throw new IllegalArgumentException("Principal is not a supported type: " + principal.getClass().getName());
    }

    private void createAuthenticationToken(CupaApiContext.CupaApiContextData contextData) {
        // Create a simple authentication token with merchant role
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            contextData.getMerchantId(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.MERCHANT))
        );

        // Set authentication details
        authentication.setDetails(contextData);

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
