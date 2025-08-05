package lt.creditco.cupa.domain;

import java.util.UUID;

public class ClientCardTestSamples {

    public static ClientCard getClientCardSample1() {
        return new ClientCard()
            .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .maskedPan("maskedPan1")
            .expiryDate("expiryDate1")
            .cardholderName("cardholderName1");
    }

    public static ClientCard getClientCardSample2() {
        return new ClientCard()
            .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
            .maskedPan("maskedPan2")
            .expiryDate("expiryDate2")
            .cardholderName("cardholderName2");
    }

    public static ClientCard getClientCardRandomSampleGenerator() {
        return new ClientCard()
            .id(UUID.randomUUID())
            .maskedPan(UUID.randomUUID().toString())
            .expiryDate(UUID.randomUUID().toString())
            .cardholderName(UUID.randomUUID().toString());
    }
}
