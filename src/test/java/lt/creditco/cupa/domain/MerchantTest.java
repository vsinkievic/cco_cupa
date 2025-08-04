package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.AuditLogTestSamples.*;
import static lt.creditco.cupa.domain.ClientTestSamples.*;
import static lt.creditco.cupa.domain.MerchantTestSamples.*;
import static lt.creditco.cupa.domain.PaymentTransactionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
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

    @Test
    void clientTest() {
        Merchant merchant = getMerchantRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        merchant.addClient(clientBack);
        assertThat(merchant.getClients()).containsOnly(clientBack);
        assertThat(clientBack.getMerchant()).isEqualTo(merchant);

        merchant.removeClient(clientBack);
        assertThat(merchant.getClients()).doesNotContain(clientBack);
        assertThat(clientBack.getMerchant()).isNull();

        merchant.clients(new HashSet<>(Set.of(clientBack)));
        assertThat(merchant.getClients()).containsOnly(clientBack);
        assertThat(clientBack.getMerchant()).isEqualTo(merchant);

        merchant.setClients(new HashSet<>());
        assertThat(merchant.getClients()).doesNotContain(clientBack);
        assertThat(clientBack.getMerchant()).isNull();
    }

    @Test
    void transactionTest() {
        Merchant merchant = getMerchantRandomSampleGenerator();
        PaymentTransaction paymentTransactionBack = getPaymentTransactionRandomSampleGenerator();

        merchant.addTransaction(paymentTransactionBack);
        assertThat(merchant.getTransactions()).containsOnly(paymentTransactionBack);
        assertThat(paymentTransactionBack.getMerchant()).isEqualTo(merchant);

        merchant.removeTransaction(paymentTransactionBack);
        assertThat(merchant.getTransactions()).doesNotContain(paymentTransactionBack);
        assertThat(paymentTransactionBack.getMerchant()).isNull();

        merchant.transactions(new HashSet<>(Set.of(paymentTransactionBack)));
        assertThat(merchant.getTransactions()).containsOnly(paymentTransactionBack);
        assertThat(paymentTransactionBack.getMerchant()).isEqualTo(merchant);

        merchant.setTransactions(new HashSet<>());
        assertThat(merchant.getTransactions()).doesNotContain(paymentTransactionBack);
        assertThat(paymentTransactionBack.getMerchant()).isNull();
    }

    @Test
    void auditLogTest() {
        Merchant merchant = getMerchantRandomSampleGenerator();
        AuditLog auditLogBack = getAuditLogRandomSampleGenerator();

        merchant.addAuditLog(auditLogBack);
        assertThat(merchant.getAuditLogs()).containsOnly(auditLogBack);
        assertThat(auditLogBack.getMerchant()).isEqualTo(merchant);

        merchant.removeAuditLog(auditLogBack);
        assertThat(merchant.getAuditLogs()).doesNotContain(auditLogBack);
        assertThat(auditLogBack.getMerchant()).isNull();

        merchant.auditLogs(new HashSet<>(Set.of(auditLogBack)));
        assertThat(merchant.getAuditLogs()).containsOnly(auditLogBack);
        assertThat(auditLogBack.getMerchant()).isEqualTo(merchant);

        merchant.setAuditLogs(new HashSet<>());
        assertThat(merchant.getAuditLogs()).doesNotContain(auditLogBack);
        assertThat(auditLogBack.getMerchant()).isNull();
    }
}
