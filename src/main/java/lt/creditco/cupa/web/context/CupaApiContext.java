package lt.creditco.cupa.web.context;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Thread-local context holder for CUPA API request data.
 * Provides access to business context extracted once per request.
 */
@Component
public class CupaApiContext {

    private static final ThreadLocal<CupaApiContextData> contextHolder = new ThreadLocal<>();

    public static void setContext(CupaApiContextData context) {
        contextHolder.set(context);
    }

    public static CupaApiContextData getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

    @Data
    @Builder
    public static class CupaApiContextData {

        private String merchantId;
        private String cupaApiKey;
        private MerchantContext merchantContext;
        private String orderId;
        private String clientId;
        private String requestData;
        private String requesterIpAddress;
        private String apiEndpoint;
        private String httpMethod;
        private Instant requestTimestamp;
        private Long auditLogId;
        private User user;

        public String getEnvironment() {
            return merchantContext != null && merchantContext.getMode() != null ? merchantContext.getMode().name() : null;
        }

        public boolean canAccessEntity(MerchantOwnedEntity entity) {
            if (entity == null) {
                return false;
            }

            // If we have a user (authenticated via JWT), use user's access logic
            if (user != null) {
                return user.canAccessEntity(entity);
            }

            // If we have a merchant context (authenticated via API key),
            // only allow access to entities of that merchant
            if (merchantId != null) {
                return merchantId.equals(entity.getMerchantId());
            }

            return false;
        }

        public void checkAccessToEntity(MerchantOwnedEntity entity) {
            if (!canAccessEntity(entity)) {
                throw new AccessDeniedException(
                    "Access denied to entity with merchant ID: " + (entity != null ? entity.getMerchantId() : "null")
                );
            }
        }
    }

    @Data
    @Builder
    public static class MerchantContext {

        private String merchantId;
        private String environment;
        private String cupaApiKey;
        private MerchantMode mode;
        private MerchantStatus status;
        private String gatewayUrl;
        private String gatewayMerchantId;
        private String gatewayMerchantKey;
        private String gatewayApiKey;
    }
}
