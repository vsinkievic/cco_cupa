package lt.creditco.cupa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClientCardDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClientCardDTO.class);
        ClientCardDTO clientCardDTO1 = new ClientCardDTO();
        clientCardDTO1.setId(1L);
        ClientCardDTO clientCardDTO2 = new ClientCardDTO();
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
        clientCardDTO2.setId(clientCardDTO1.getId());
        assertThat(clientCardDTO1).isEqualTo(clientCardDTO2);
        clientCardDTO2.setId(2L);
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
        clientCardDTO1.setId(null);
        assertThat(clientCardDTO1).isNotEqualTo(clientCardDTO2);
    }
}
