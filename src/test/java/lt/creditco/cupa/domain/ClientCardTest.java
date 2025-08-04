package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.ClientCardTestSamples.*;
import static lt.creditco.cupa.domain.ClientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClientCardTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClientCard.class);
        ClientCard clientCard1 = getClientCardSample1();
        ClientCard clientCard2 = new ClientCard();
        assertThat(clientCard1).isNotEqualTo(clientCard2);

        clientCard2.setId(clientCard1.getId());
        assertThat(clientCard1).isEqualTo(clientCard2);

        clientCard2 = getClientCardSample2();
        assertThat(clientCard1).isNotEqualTo(clientCard2);
    }

    @Test
    void clientTest() {
        ClientCard clientCard = getClientCardRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        clientCard.setClient(clientBack);
        assertThat(clientCard.getClient()).isEqualTo(clientBack);

        clientCard.client(null);
        assertThat(clientCard.getClient()).isNull();
    }
}
