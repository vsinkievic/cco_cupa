package lt.creditco.cupa.web.context;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.DailyAmountLimit;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.bpmid.vapp.domain.User;

/**
 * Thread-local context holder for CUPA API request data.
 * Provides access to business context extracted once per request.
 */
@Component
@Slf4j
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
        private CupaUser cupaUser;

        public CupaUser getCupaUser() {
            if (cupaUser == null && user != null && user instanceof CupaUser) {
                cupaUser = (CupaUser) user;
            }
            return cupaUser;
        }

        public MerchantMode getEnvironment() {
            return merchantContext != null ? merchantContext.getMode() : null;
        }

        public String getMerchantId() {
            return merchantContext != null && merchantContext.getMerchantId() != null ? merchantContext.getMerchantId() : merchantId;
        }

        public boolean canAccessEntity(MerchantOwnedEntity entity) {
            if (entity == null) {
                return false;
            }

            if (cupaUser == null && user != null && user instanceof CupaUser) {
                cupaUser = (CupaUser) user;
            }
            // If we have a user (authenticated via JWT), use user's access logic
            if (cupaUser != null) {
                return cupaUser.canAccessEntity(entity);
            }

            // If we have a merchant context (authenticated via API key),
            // only allow access to entities of that merchant
            String contextMerchantId = getMerchantId();
            if (contextMerchantId != null) {
                if (!contextMerchantId.equals(entity.getMerchantId())) {
                    return false;
                }
                if (this.merchantContext == null)
                    return false;
                if (!this.merchantContext.getMerchantId().equals(entity.getMerchantId())) {
                    return false;
                }
                if (!MerchantStatus.ACTIVE.equals(this.merchantContext.getStatus())) {
                    return false;
                }
                return true;
            }
            return false;
        }
        public boolean isAuthenticated(){
            if (merchantContext == null) return false;
            if (merchantContext.getMerchantId() == null) return false;
            if (!MerchantStatus.ACTIVE.equals(merchantContext.getStatus())) return false;

            return true;
        }

        public void checkAccessToEntity(MerchantOwnedEntity entity) {
            log.debug("checkAccessToEntity({} {}) in the context of {}", entity.getClass().getSimpleName(), entity == null ? "null" : entity, this);

            if (!canAccessEntity(entity)) {
                log.warn("Access denied to entity with merchant ID: {}, context: {}", entity != null ? entity.getMerchantId() : "null", this);
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
        private MerchantMode environment;
        private String cupaApiKey;
        private MerchantMode mode;
        private MerchantStatus status;
        private String gatewayUrl;
        private String gatewayMerchantId;
        private String gatewayMerchantKey;
        private String gatewayApiKey;
        private String securityRemarks;
        private String clientIdPrefix;
        private String orderIdPrefix;
        private DailyAmountLimit dailyAmountLimit;

        public boolean satisfiesClientIdPrefix(String clientId) {
            if (clientIdPrefix == null) return true;
            if (clientId == null) return false;
            return clientId.startsWith(clientIdPrefix);
        }
        public boolean satisfiesOrderIdPrefix(String orderId) {
            if (orderIdPrefix == null) return true;
            if (orderId == null) return false;
            return orderId.startsWith(orderIdPrefix);
        }
    }
}
