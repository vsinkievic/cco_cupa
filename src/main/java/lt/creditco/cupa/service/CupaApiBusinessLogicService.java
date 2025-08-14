package lt.creditco.cupa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.UserRepository;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepository userRepo;
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

        String requestApiKey = getApiKeyFromHeaders(request);
        contextBuilder.cupaApiKey(requestApiKey);

        User user = null;
        if (principal != null) {
            user = userRepo.findOneWithAuthoritiesByLogin(principal.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            contextBuilder.user(user);
        }

        // Determine merchant and environment based on authentication
        CupaApiContext.MerchantContext merchantContext = determineMerchantContext(requestApiKey, user);
        contextBuilder.merchantContext(merchantContext);

        // Extract orderId from path variables or request body
        String orderId = extractOrderId(request, requestBody);
        contextBuilder.orderId(orderId);

        // Extract clientId from request body
        String clientId = extractClientId(requestBody);
        contextBuilder.clientId(clientId);

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
        log.debug("Path info: {}", pathInfo);
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

    private CupaApiContext.MerchantContext determineMerchantContext(String requestApiKey, User user) {
        if (requestApiKey == null && user == null) {
            log.warn("No API key or principal available for merchant context determination");
            return getDefaultMerchantContext();
        }

        Merchant merchant = null;
        if (requestApiKey != null) {
            merchant = merchantService.findMerchantByCupaApiKey(requestApiKey);
        }
        if (user != null) {
            String[] merchantIds = user.getMerchantIds() != null ? user.getMerchantIds().split(",") : new String[0];
            if (merchant == null) {
                if (merchantIds.length == 1) {
                    merchant = merchantService.findMerchantById(merchantIds[0]);
                } else {
                    log.warn(
                        "No merchant found for user: {}, merchantIds: {}, returning null values",
                        user.getLogin(),
                        user.getMerchantIds()
                    );
                    return getDefaultMerchantContext();
                }
            } else {
                if (!StringUtils.containsAny(merchant.getId(), merchantIds)) {
                    throw new RuntimeException("Merchant (API key) not allowed to use with user " + user.getLogin());
                }
            }
        }

        if (merchant == null) {
            log.warn("No merchant found for API key: {}, returning null values", requestApiKey);
            return getDefaultMerchantContext();
        }

        String gatewayUrl = null;
        String gatewayMerchantId = null;
        String gatewayMerchantKey = null;
        String gatewayApiKey = null;

        if (merchant.getMode() == MerchantMode.LIVE) {
            gatewayUrl = merchant.getRemoteProdUrl();
            gatewayMerchantId = merchant.getRemoteProdMerchantId();
            gatewayMerchantKey = merchant.getRemoteProdMerchantKey();
            gatewayApiKey = merchant.getRemoteProdApiKey();
        } else {
            gatewayUrl = merchant.getRemoteTestUrl();
            gatewayMerchantId = merchant.getRemoteTestMerchantId();
            gatewayMerchantKey = merchant.getRemoteTestMerchantKey();
            gatewayApiKey = merchant.getRemoteTestApiKey();
        }
        return CupaApiContext.MerchantContext.builder()
            .merchantId(merchant.getId())
            .environment(merchant.getMode().name())
            .cupaApiKey(requestApiKey)
            .mode(merchant.getMode())
            .status(merchant.getStatus())
            .gatewayUrl(gatewayUrl)
            .gatewayMerchantId(gatewayMerchantId)
            .gatewayMerchantKey(gatewayMerchantKey)
            .gatewayApiKey(gatewayApiKey)
            .build();
    }

    private String getApiKeyFromHeaders(HttpServletRequest request) {
        return request.getHeader("X-API-Key");
    }

    private CupaApiContext.MerchantContext findMerchantByApiKey(String apiKey) {
        // This is a simplified implementation
        // In a real scenario, you would query the database to find the merchant by API key
        log.debug("Looking up merchant by API key: {}", apiKey);

        // For now, we'll check if the API key matches any known test keys
        if ("test_key_123".equals(apiKey)) {
            return CupaApiContext.MerchantContext.builder().merchantId("MERCH001").environment("TEST").cupaApiKey(apiKey).build();
        } else if ("test_key_456".equals(apiKey)) {
            return CupaApiContext.MerchantContext.builder().merchantId("MERCH002").environment("TEST").cupaApiKey(apiKey).build();
        }

        log.debug("No merchant found for API key: {}, returning null values", apiKey);
        return getDefaultMerchantContext();
    }

    private CupaApiContext.MerchantContext getDefaultMerchantContext() {
        return CupaApiContext.MerchantContext.builder()
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
            if (StringUtils.isNotBlank(queryString)) {
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
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
