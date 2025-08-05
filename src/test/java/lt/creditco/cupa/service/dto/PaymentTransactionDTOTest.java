package lt.creditco.cupa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentTransactionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PaymentTransactionDTO.class);
        PaymentTransactionDTO paymentTransactionDTO1 = new PaymentTransactionDTO();
        paymentTransactionDTO1.setId("11111111-1111-1111-1111-111111111111");
        PaymentTransactionDTO paymentTransactionDTO2 = new PaymentTransactionDTO();
        assertThat(paymentTransactionDTO1).isNotEqualTo(paymentTransactionDTO2);
        paymentTransactionDTO2.setId(paymentTransactionDTO1.getId());
        assertThat(paymentTransactionDTO1).isEqualTo(paymentTransactionDTO2);
        paymentTransactionDTO2.setId("22222222-2222-2222-2222-222222222222");
        assertThat(paymentTransactionDTO1).isNotEqualTo(paymentTransactionDTO2);
        paymentTransactionDTO1.setId(null);
        assertThat(paymentTransactionDTO1).isNotEqualTo(paymentTransactionDTO2);
    }
}
