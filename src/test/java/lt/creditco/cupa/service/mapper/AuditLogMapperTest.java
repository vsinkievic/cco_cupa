package lt.creditco.cupa.service.mapper;

import static lt.creditco.cupa.domain.AuditLogAsserts.*;
import static lt.creditco.cupa.domain.AuditLogTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditLogMapperTest {

    private AuditLogMapper auditLogMapper;

    @BeforeEach
    void setUp() {
        auditLogMapper = new AuditLogMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAuditLogSample1();
        var actual = auditLogMapper.toEntity(auditLogMapper.toDto(expected));
        assertAuditLogAllPropertiesEquals(expected, actual);
    }
}
