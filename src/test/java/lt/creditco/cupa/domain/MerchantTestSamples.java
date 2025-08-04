package lt.creditco.cupa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MerchantTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Merchant getMerchantSample1() {
        return new Merchant()
            .id(1L)
            .name("name1")
            .cupaTestApiKey("cupaTestApiKey1")
            .cupaProdApiKey("cupaProdApiKey1")
            .remoteTestUrl("remoteTestUrl1")
            .remoteTestMerchantId("remoteTestMerchantId1")
            .remoteTestMerchantKey("remoteTestMerchantKey1")
            .remoteTestApiKey("remoteTestApiKey1")
            .remoteProdUrl("remoteProdUrl1")
            .remoteProdMerchantId("remoteProdMerchantId1")
            .remoteProdMerchantKey("remoteProdMerchantKey1")
            .remoteProdApiKey("remoteProdApiKey1");
    }

    public static Merchant getMerchantSample2() {
        return new Merchant()
            .id(2L)
            .name("name2")
            .cupaTestApiKey("cupaTestApiKey2")
            .cupaProdApiKey("cupaProdApiKey2")
            .remoteTestUrl("remoteTestUrl2")
            .remoteTestMerchantId("remoteTestMerchantId2")
            .remoteTestMerchantKey("remoteTestMerchantKey2")
            .remoteTestApiKey("remoteTestApiKey2")
            .remoteProdUrl("remoteProdUrl2")
            .remoteProdMerchantId("remoteProdMerchantId2")
            .remoteProdMerchantKey("remoteProdMerchantKey2")
            .remoteProdApiKey("remoteProdApiKey2");
    }

    public static Merchant getMerchantRandomSampleGenerator() {
        return new Merchant()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .cupaTestApiKey(UUID.randomUUID().toString())
            .cupaProdApiKey(UUID.randomUUID().toString())
            .remoteTestUrl(UUID.randomUUID().toString())
            .remoteTestMerchantId(UUID.randomUUID().toString())
            .remoteTestMerchantKey(UUID.randomUUID().toString())
            .remoteTestApiKey(UUID.randomUUID().toString())
            .remoteProdUrl(UUID.randomUUID().toString())
            .remoteProdMerchantId(UUID.randomUUID().toString())
            .remoteProdMerchantKey(UUID.randomUUID().toString())
            .remoteProdApiKey(UUID.randomUUID().toString());
    }
}
