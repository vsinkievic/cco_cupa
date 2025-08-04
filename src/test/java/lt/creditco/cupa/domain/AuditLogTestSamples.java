package lt.creditco.cupa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AuditLogTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static AuditLog getAuditLogSample1() {
        return new AuditLog()
            .id(1L)
            .apiEndpoint("apiEndpoint1")
            .httpMethod("httpMethod1")
            .httpStatusCode(1)
            .orderId("orderId1")
            .responseDescription("responseDescription1")
            .cupaApiKey("cupaApiKey1")
            .environment("environment1")
            .requesterIpAddress("requesterIpAddress1");
    }

    public static AuditLog getAuditLogSample2() {
        return new AuditLog()
            .id(2L)
            .apiEndpoint("apiEndpoint2")
            .httpMethod("httpMethod2")
            .httpStatusCode(2)
            .orderId("orderId2")
            .responseDescription("responseDescription2")
            .cupaApiKey("cupaApiKey2")
            .environment("environment2")
            .requesterIpAddress("requesterIpAddress2");
    }

    public static AuditLog getAuditLogRandomSampleGenerator() {
        return new AuditLog()
            .id(longCount.incrementAndGet())
            .apiEndpoint(UUID.randomUUID().toString())
            .httpMethod(UUID.randomUUID().toString())
            .httpStatusCode(intCount.incrementAndGet())
            .orderId(UUID.randomUUID().toString())
            .responseDescription(UUID.randomUUID().toString())
            .cupaApiKey(UUID.randomUUID().toString())
            .environment(UUID.randomUUID().toString())
            .requesterIpAddress(UUID.randomUUID().toString());
    }
}
