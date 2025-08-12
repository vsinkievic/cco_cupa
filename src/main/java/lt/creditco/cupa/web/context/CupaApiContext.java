package lt.creditco.cupa.web.context;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;
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
        private String environment;
        private String cupaApiKey;
        private String orderId;
        private String clientId;
        private String requestData;
        private String requesterIpAddress;
        private String apiEndpoint;
        private String httpMethod;
        private Instant requestTimestamp;
        private Long auditLogId;
    }
}
