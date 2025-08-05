package lt.creditco.cupa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClientCardDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClientCardDTO.class);
        ClientCardDTO clientCardDTO1 = new ClientCardDTO();
        clientCardDTO1.setId("11111111-1111-1111-1111-111111111111");
        ClientCardDTO clientCardDTO2 = new ClientCardDTO();
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
        clientCardDTO2.setId(clientCardDTO1.getId());
        assertThat(clientCardDTO1).isEqualTo(clientCardDTO2);
        clientCardDTO2.setId("22222222-2222-2222-2222-222222222222");
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
        clientCardDTO1.setId(null);
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
    }
}
