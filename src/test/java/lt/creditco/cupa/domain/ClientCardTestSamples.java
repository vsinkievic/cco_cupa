package lt.creditco.cupa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ClientCardTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ClientCard getClientCardSample1() {
        return new ClientCard().id(1L).maskedPan("maskedPan1").expiryDate("expiryDate1").cardholderName("cardholderName1");
    }

    public static ClientCard getClientCardSample2() {
        return new ClientCard().id(2L).maskedPan("maskedPan2").expiryDate("expiryDate2").cardholderName("cardholderName2");
    }

    public static ClientCard getClientCardRandomSampleGenerator() {
        return new ClientCard()
            .id(longCount.incrementAndGet())
            .maskedPan(UUID.randomUUID().toString())
            .expiryDate(UUID.randomUUID().toString())
            .cardholderName(UUID.randomUUID().toString());
    }
}
