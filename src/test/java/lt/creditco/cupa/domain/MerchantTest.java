package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.MerchantTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MerchantTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Merchant.class);
        Merchant merchant1 = getMerchantSample1();
        Merchant merchant2 = new Merchant();
        assertThat(merchant1).isNotEqualTo(merchant2);

        merchant2.setId(merchant1.getId());
        assertThat(merchant1).isEqualTo(merchant2);

        merchant2 = getMerchantSample2();
        assertThat(merchant1).isNotEqualTo(merchant2);
    }
}
