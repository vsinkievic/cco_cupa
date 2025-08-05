package lt.creditco.cupa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MerchantDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MerchantDTO.class);
        MerchantDTO merchantDTO1 = new MerchantDTO();
        merchantDTO1.setId("11111111-1111-1111-1111-111111111111");
        MerchantDTO merchantDTO2 = new MerchantDTO();
        assertThat(merchantDTO1).isNotEqualTo(merchantDTO2);
        merchantDTO2.setId(merchantDTO1.getId());
        assertThat(merchantDTO1).isEqualTo(merchantDTO2);
        merchantDTO2.setId("22222222-2222-2222-2222-222222222222");
        assertThat(merchantDTO1).isNotEqualTo(merchantDTO2);
        merchantDTO1.setId(null);
        assertThat(merchantDTO1).isNotEqualTo(merchantDTO2);
    }
}
