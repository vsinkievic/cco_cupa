package lt.creditco.cupa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service for extracting business context from CUPA API requests.
 * Ensures business logic runs only once per request.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CupaApiBusinessLogicService {

    private final MerchantService merchantService;
    private final ObjectMapper objectMapper;

    /**
     * Extract and determine all business context from the request.
     * This runs once per request and provides data for both audit logging and business logic.
     */
    public CupaApiContext.CupaApiContextData extractBusinessContext(HttpServletRequest request, Object requestBody, Principal principal) {
        CupaApiContext.CupaApiContextData.CupaApiContextDataBuilder contextBuilder = CupaApiContext.CupaApiContextData.builder()
            .requestTimestamp(Instant.now())
            .apiEndpoint(request.getRequestURI())
            .httpMethod(request.getMethod())
            .requesterIpAddress(getClientIpAddress(request));

        // Extract orderId from path variables or request body
        String orderId = extractOrderId(request, requestBody);
        contextBuilder.orderId(orderId);

        // Extract clientId from request body
        String clientId = extractClientId(requestBody);
        contextBuilder.clientId(clientId);

        // Determine merchant and environment based on authentication
        MerchantContext merchantContext = determineMerchantContext(principal);
        contextBuilder
            .merchantId(merchantContext.getMerchantId())
            .environment(merchantContext.getEnvironment())
            .cupaApiKey(merchantContext.getCupaApiKey());

        // Extract request data
        String requestData = extractRequestData(request, requestBody);
        contextBuilder.requestData(requestData);

        return contextBuilder.build();
    }

    private String extractOrderId(HttpServletRequest request, Object requestBody) {
        // Try path variables first
        String orderId = extractFromPathVariables(request, "orderId");
        if (orderId != null) {
            return orderId;
        }

        // Try request body
        if (requestBody instanceof PaymentRequest paymentRequest) {
            return paymentRequest.getOrderId();
        }

        return null;
    }

    private String extractClientId(Object requestBody) {
        if (requestBody instanceof PaymentRequest paymentRequest) {
            return paymentRequest.getClientId();
        }
        return null;
    }

    private String extractFromPathVariables(HttpServletRequest request, String variableName) {
        // This is a simplified implementation
        // In a real scenario, you might need to parse the path more carefully
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/" + variableName + "/")) {
            String[] pathParts = pathInfo.split("/");
            for (int i = 0; i < pathParts.length - 1; i++) {
                if (variableName.equals(pathParts[i])) {
                    return pathParts[i + 1];
                }
            }
        }
        return null;
    }

    private MerchantContext determineMerchantContext(Principal principal) {
        if (principal == null) {
            log.warn("No principal available for merchant context determination");
            return getDefaultMerchantContext();
        }

        String username = principal.getName();
        log.debug("Determining merchant context for user: {}", username);

        // Option 1: Extract from username format (e.g., "merchant1_test")
        if (username.contains("_")) {
            String[] parts = username.split("_");
            if (parts.length >= 2) {
                String merchantId = parts[0];
                String environment = parts[1].toUpperCase();

                // Get merchant details from database
                Optional<MerchantDTO> merchant = merchantService.findOne(merchantId);
                if (merchant.isPresent()) {
                    String apiKey = "TEST".equals(environment) ? merchant.get().getCupaTestApiKey() : merchant.get().getCupaProdApiKey();

                    return MerchantContext.builder().merchantId(merchantId).environment(environment).cupaApiKey(apiKey).build();
                } else {
                    log.debug("Merchant with ID '{}' not found, returning null values", merchantId);
                }
            }
        }

        // Option 2: Try to find merchant by API key from headers
        String apiKeyFromHeader = getApiKeyFromHeaders();
        if (StringUtils.hasText(apiKeyFromHeader)) {
            // You would implement a method to find merchant by API key
            // For now, we'll use a simplified approach
            return findMerchantByApiKey(apiKeyFromHeader);
        }

        // Option 3: Default fallback
        return getDefaultMerchantContext();
    }

    private String getApiKeyFromHeaders() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-API-Key");
        }
        return null;
    }

    private MerchantContext findMerchantByApiKey(String apiKey) {
        // This is a simplified implementation
        // In a real scenario, you would query the database to find the merchant by API key
        log.debug("Looking up merchant by API key: {}", apiKey);

        // For now, we'll check if the API key matches any known test keys
        if ("test_key_123".equals(apiKey)) {
            return MerchantContext.builder().merchantId("MERCH001").environment("TEST").cupaApiKey(apiKey).build();
        } else if ("test_key_456".equals(apiKey)) {
            return MerchantContext.builder().merchantId("MERCH002").environment("TEST").cupaApiKey(apiKey).build();
        }

        log.debug("No merchant found for API key: {}, returning null values", apiKey);
        return getDefaultMerchantContext();
    }

    private MerchantContext getDefaultMerchantContext() {
        return MerchantContext.builder()
            .merchantId(null) // Don't set default values when we can't determine them
            .environment(null)
            .cupaApiKey(null)
            .build();
    }

    private String extractRequestData(HttpServletRequest request, Object requestBody) {
        try {
            if (requestBody != null) {
                return objectMapper.writeValueAsString(requestBody);
            }

            // If no request body, try to extract from query parameters
            String queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                return "Query: " + queryString;
            }

            return null;
        } catch (Exception e) {
            log.warn("Error extracting request data", e);
            return "Error extracting request data: " + e.getMessage();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @Data
    @Builder
    public static class MerchantContext {

        private String merchantId;
        private String environment;
        private String cupaApiKey;
    }
}
