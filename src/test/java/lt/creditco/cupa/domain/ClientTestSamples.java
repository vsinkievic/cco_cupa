package lt.creditco.cupa.domain;

import java.util.UUID;

public class ClientTestSamples {

    public static Client getClientSample1() {
        return new Client()
            .id("11111111-1111-1111-1111-111111111111")
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
            .id("22222222-2222-2222-2222-222222222222")
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
            .id(UUID.randomUUID().toString())
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
