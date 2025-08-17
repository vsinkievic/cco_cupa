package lt.creditco.cupa.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter for API key authentication.
 * Reads X-API-KEY header and validates it against merchant API keys.
 * If validation fails, continues with normal JWT authentication.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_V1_PATH = "/api/v1";

    private final MerchantRepository merchantRepository;

    public ApiKeyAuthenticationFilter(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // Only process API v1 requests
        if (!request.getRequestURI().startsWith(API_V1_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(Constants.API_KEY_HEADER);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            // No API key provided, continue with normal authentication
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Try to authenticate with API key
            if (authenticateWithApiKey(apiKey, request)) {
                LOG.debug("API key authentication successful for request: {}", request.getRequestURI());
            } else {
                LOG.debug("API key authentication failed for request: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            LOG.warn("Error during API key authentication: {}", e.getMessage());
            // Continue with normal authentication on error
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the context after request processing
            CupaApiContext.clearContext();
        }
    }

    /**
     * Authenticate using API key.
     *
     * @param apiKey the API key from header
     * @param request the HTTP request
     * @return true if authentication was successful, false otherwise
     */
    private boolean authenticateWithApiKey(String apiKey, HttpServletRequest request) {
        // First try to find merchant by production API key
        Merchant merchant = merchantRepository.findOneByCupaProdApiKey(apiKey).orElse(null);

        if (merchant != null) {
            if (validateMerchant(merchant, MerchantMode.LIVE, request)) {
                return true;
            }
        }

        // If not found or invalid, try test API key
        merchant = merchantRepository.findOneByCupaTestApiKey(apiKey).orElse(null);
        if (merchant != null) {
            if (validateMerchant(merchant, MerchantMode.TEST, request)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate merchant for authentication.
     *
     * @param merchant the merchant to validate
     * @param expectedMode the expected mode for this API key
     * @param request the HTTP request
     * @return true if merchant is valid for authentication, false otherwise
     */
    private boolean validateMerchant(Merchant merchant, MerchantMode expectedMode, HttpServletRequest request) {
        // Check if merchant status is ACTIVE
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            LOG.debug("Merchant {} is not active (status: {})", merchant.getId(), merchant.getStatus());
            return false;
        }

        // Check if merchant mode matches the expected mode for this API key
        if (merchant.getMode() != expectedMode) {
            LOG.debug("Merchant {} mode mismatch. Expected: {}, Actual: {}", merchant.getId(), expectedMode, merchant.getMode());
            return false;
        }

        // All validations passed, create authentication token
        createAuthenticationToken(merchant, expectedMode);

        // Set merchant context for the request
        setMerchantContext(merchant, expectedMode);

        return true;
    }

    /**
     * Create and set the authentication token.
     *
     * @param merchant the authenticated merchant
     * @param mode the merchant mode
     */
    private void createAuthenticationToken(Merchant merchant, MerchantMode mode) {
        // Create a simple authentication token with merchant role
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            merchant.getId(),
            null,
            Collections.emptyList() //            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MERCHANT"))
        );

        // Set authentication details
        authentication.setDetails(merchant);

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Set merchant context for the request.
     *
     * @param merchant the authenticated merchant
     * @param mode the merchant mode
     */
    private void setMerchantContext(Merchant merchant, MerchantMode mode) {
        // Create merchant context
        CupaApiContext.MerchantContext merchantContext = CupaApiContext.MerchantContext.builder()
            .merchantId(merchant.getId())
            .environment(mode.name())
            .cupaApiKey(mode == MerchantMode.TEST ? merchant.getCupaTestApiKey() : merchant.getCupaProdApiKey())
            .mode(mode)
            .status(merchant.getStatus())
            .build();

        // Create API context data
        CupaApiContext.CupaApiContextData contextData = CupaApiContext.CupaApiContextData.builder()
            .merchantId(merchant.getId())
            .cupaApiKey(mode == MerchantMode.TEST ? merchant.getCupaTestApiKey() : merchant.getCupaProdApiKey())
            .merchantContext(merchantContext)
            .build();

        // Set the context
        CupaApiContext.setContext(contextData);
    }
}
