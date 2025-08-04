package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.ClientTestSamples.*;
import static lt.creditco.cupa.domain.MerchantTestSamples.*;
import static lt.creditco.cupa.domain.PaymentTransactionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentTransactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PaymentTransaction.class);
        PaymentTransaction paymentTransaction1 = getPaymentTransactionSample1();
        PaymentTransaction paymentTransaction2 = new PaymentTransaction();
        assertThat(paymentTransaction1).isNotEqualTo(paymentTransaction2);

        paymentTransaction2.setId(paymentTransaction1.getId());
        assertThat(paymentTransaction1).isEqualTo(paymentTransaction2);

        paymentTransaction2 = getPaymentTransactionSample2();
        assertThat(paymentTransaction1).isNotEqualTo(paymentTransaction2);
    }

    @Test
    void clientTest() {
        PaymentTransaction paymentTransaction = getPaymentTransactionRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        paymentTransaction.setClient(clientBack);
        assertThat(paymentTransaction.getClient()).isEqualTo(clientBack);

        paymentTransaction.client(null);
        assertThat(paymentTransaction.getClient()).isNull();
    }

    @Test
    void merchantTest() {
        PaymentTransaction paymentTransaction = getPaymentTransactionRandomSampleGenerator();
        Merchant merchantBack = getMerchantRandomSampleGenerator();

        paymentTransaction.setMerchant(merchantBack);
        assertThat(paymentTransaction.getMerchant()).isEqualTo(merchantBack);

        paymentTransaction.merchant(null);
        assertThat(paymentTransaction.getMerchant()).isNull();
    }
}
