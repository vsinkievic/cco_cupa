package lt.creditco.cupa.domain;

import static lt.creditco.cupa.domain.AuditLogTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lt.creditco.cupa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuditLog.class);
        AuditLog auditLog1 = getAuditLogSample1();
        AuditLog auditLog2 = new AuditLog();
        assertThat(auditLog1).isNotEqualTo(auditLog2);

        auditLog2.setId(auditLog1.getId());
        assertThat(auditLog1).isEqualTo(auditLog2);

        auditLog2 = getAuditLogSample2();
        assertThat(auditLog1).isNotEqualTo(auditLog2);
    }

    @Test
    void merchantTest() {
        AuditLog auditLog = getAuditLogRandomSampleGenerator();
        String merchantBackId = UUID.randomUUID().toString();

        auditLog.setMerchantId(merchantBackId);
        assertThat(auditLog.getMerchantId()).isEqualTo(merchantBackId);

        auditLog.setMerchantId(null);
        assertThat(auditLog.getMerchantId()).isNull();
    }
}
