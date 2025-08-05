package lt.creditco.cupa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentTransactionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static PaymentTransaction getPaymentTransactionSample1() {
        return new PaymentTransaction()
            .id("11111111-1111-1111-1111-111111111111")
            .merchantId("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")
            .clientId("clientId1")
            .gatewayTransactionId("gatewayTransactionId1")
            .statusDescription("statusDescription1")
            .replyUrl("replyUrl1")
            .backofficeUrl("backofficeUrl1")
            .echo("echo1")
            .signature("signature1")
            .signatureVersion("signatureVersion1");
    }

    public static PaymentTransaction getPaymentTransactionSample2() {
        return new PaymentTransaction()
            .id("22222222-2222-2222-2222-222222222222")
            .merchantId("merchantId2")
            .clientId("clientId2")
            .gatewayTransactionId("gatewayTransactionId2")
            .statusDescription("statusDescription2")
            .replyUrl("replyUrl2")
            .backofficeUrl("backofficeUrl2")
            .echo("echo2")
            .signature("signature2")
            .signatureVersion("signatureVersion2");
    }

    public static PaymentTransaction getPaymentTransactionRandomSampleGenerator() {
        return new PaymentTransaction()
            .id(UUID.randomUUID().toString())
            .merchantId(UUID.randomUUID().toString())
            .clientId(UUID.randomUUID().toString())
            .gatewayTransactionId(UUID.randomUUID().toString())
            .statusDescription(UUID.randomUUID().toString())
            .replyUrl(UUID.randomUUID().toString())
            .backofficeUrl(UUID.randomUUID().toString())
            .echo(UUID.randomUUID().toString())
            .signature(UUID.randomUUID().toString())
            .signatureVersion(UUID.randomUUID().toString());
    }
}
