package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.ClientCardTestSamples.*;
import static lt.creditco.cupa.domain.ClientTestSamples.*;
import static lt.creditco.cupa.domain.MerchantTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Client.class);
        Client client1 = getClientSample1();
        Client client2 = new Client();
        assertThat(client1).isNotEqualTo(client2);

        client2.setId(client1.getId());
        assertThat(client1).isEqualTo(client2);

        client2 = getClientSample2();
        assertThat(client1).isNotEqualTo(client2);
    }

    @Test
    void cardTest() {
        Client client = getClientRandomSampleGenerator();
        ClientCard clientCardBack = getClientCardRandomSampleGenerator();

        client.addCard(clientCardBack);
        assertThat(client.getCards()).containsOnly(clientCardBack);
        assertThat(clientCardBack.getClient()).isEqualTo(client);

        client.removeCard(clientCardBack);
        assertThat(client.getCards()).doesNotContain(clientCardBack);
        assertThat(clientCardBack.getClient()).isNull();

        client.cards(new HashSet<>(Set.of(clientCardBack)));
        assertThat(client.getCards()).containsOnly(clientCardBack);
        assertThat(clientCardBack.getClient()).isEqualTo(client);

        client.setCards(new HashSet<>());
        assertThat(client.getCards()).doesNotContain(clientCardBack);
        assertThat(clientCardBack.getClient()).isNull();
    }

    @Test
    void merchantTest() {
        Client client = getClientRandomSampleGenerator();
        String merchantBackId = UUID.randomUUID().toString();

        client.setMerchantId(merchantBackId);
        assertThat(client.getMerchantId()).isEqualTo(merchantBackId);

        client.setMerchantId(null);
        assertThat(client.getMerchantId()).isNull();
    }
}
