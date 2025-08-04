package lt.creditco.cupa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Client getClientSample1() {
        return new Client()
            .id(1L)
            .merchantClientId("merchantClientId1")
            .name("name1")
            .emailAddress("emailAddress1")
            .mobileNumber("mobileNumber1")
            .clientPhone("clientPhone1")
            .streetNumber("streetNumber1")
            .streetName("streetName1")
            .streetSuffix("streetSuffix1")
            .city("city1")
            .state("state1")
            .postCode("postCode1")
            .country("country1");
    }

    public static Client getClientSample2() {
        return new Client()
            .id(2L)
            .merchantClientId("merchantClientId2")
            .name("name2")
            .emailAddress("emailAddress2")
            .mobileNumber("mobileNumber2")
            .clientPhone("clientPhone2")
            .streetNumber("streetNumber2")
            .streetName("streetName2")
            .streetSuffix("streetSuffix2")
            .city("city2")
            .state("state2")
            .postCode("postCode2")
            .country("country2");
    }

    public static Client getClientRandomSampleGenerator() {
        return new Client()
            .id(longCount.incrementAndGet())
            .merchantClientId(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .emailAddress(UUID.randomUUID().toString())
            .mobileNumber(UUID.randomUUID().toString())
            .clientPhone(UUID.randomUUID().toString())
            .streetNumber(UUID.randomUUID().toString())
            .streetName(UUID.randomUUID().toString())
            .streetSuffix(UUID.randomUUID().toString())
            .city(UUID.randomUUID().toString())
            .state(UUID.randomUUID().toString())
            .postCode(UUID.randomUUID().toString())
            .country(UUID.randomUUID().toString());
    }
}
