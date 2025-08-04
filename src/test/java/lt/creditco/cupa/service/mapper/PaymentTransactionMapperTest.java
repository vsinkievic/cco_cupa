package lt.creditco.cupa.service.mapper;

import static lt.creditco.cupa.domain.PaymentTransactionAsserts.*;
import static lt.creditco.cupa.domain.PaymentTransactionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentTransactionMapperTest {

    private PaymentTransactionMapper paymentTransactionMapper;

    @BeforeEach
    void setUp() {
        paymentTransactionMapper = new PaymentTransactionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPaymentTransactionSample1();
        var actual = paymentTransactionMapper.toEntity(paymentTransactionMapper.toDto(expected));
        assertPaymentTransactionAllPropertiesEquals(expected, actual);
    }
}
