package lt.creditco.cupa.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = Mappers.getMapper(PaymentMapper.class);
    }

    @Test
    void shouldMapPaymentTransactionDTOToPayment() {
        // Given
        PaymentTransactionDTO dto = new PaymentTransactionDTO();
        dto.setId("test-id");
        dto.setOrderId("test-order-id");
        dto.setMerchantClientId("test-client-id");
        dto.setAmount(new BigDecimal("100.50"));
        dto.setCurrency(Currency.USD);
        dto.setStatus(TransactionStatus.SUCCESS);
        dto.setStatusDescription("Payment successful");
        dto.setRequestTimestamp(Instant.now());

        // When
        Payment payment = paymentMapper.toPayment(dto);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getId()).isEqualTo("test-id");
        assertThat(payment.getOrderId()).isEqualTo("test-order-id");
        assertThat(payment.getClientId()).isEqualTo("test-client-id");
        assertThat(payment.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(payment.getCurrency()).isEqualTo("USD");
        assertThat(payment.getStatus()).isEqualTo("SUCCESS");
        assertThat(payment.getStatusDescription()).isEqualTo("Payment successful");
        assertThat(payment.getCreatedAt()).isEqualTo(dto.getRequestTimestamp());
    }

    @Test
    void shouldMapPaymentToPaymentTransactionDTO() {
        // Given
        Payment payment = new Payment();
        payment.setId("test-id");
        payment.setOrderId("test-order-id");
        payment.setClientId("test-client-id");
        payment.setAmount(new BigDecimal("100.50"));
        payment.setCurrency("USD");
        payment.setStatus("SUCCESS");
        payment.setStatusDescription("Payment successful");
        payment.setCreatedAt(Instant.now());

        // When
        PaymentTransactionDTO dto = paymentMapper.toPaymentTransactionDTO(payment);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("test-id");
        assertThat(dto.getOrderId()).isEqualTo("test-order-id");
        assertThat(dto.getMerchantClientId()).isEqualTo("test-client-id");
        assertThat(dto.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(dto.getRequestTimestamp()).isEqualTo(payment.getCreatedAt());
        // Note: currency and status are not mapped back as they are enums in DTO but strings in Payment
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        PaymentTransactionDTO dto = new PaymentTransactionDTO();
        dto.setId("test-id");
        dto.setOrderId("test-order-id");
        // Other fields are null

        // When
        Payment payment = paymentMapper.toPayment(dto);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getId()).isEqualTo("test-id");
        assertThat(payment.getOrderId()).isEqualTo("test-order-id");
        assertThat(payment.getClientId()).isNull();
        assertThat(payment.getAmount()).isNull();
        assertThat(payment.getCurrency()).isNull();
        assertThat(payment.getStatus()).isNull();
        assertThat(payment.getStatusDescription()).isNull();
        assertThat(payment.getCreatedAt()).isNull();
    }

    @Test
    void shouldHandleNullEnumValues() {
        // Given
        PaymentTransactionDTO dto = new PaymentTransactionDTO();
        dto.setId("test-id");
        dto.setOrderId("test-order-id");
        dto.setCurrency(null);
        dto.setStatus(null);

        // When
        Payment payment = paymentMapper.toPayment(dto);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getCurrency()).isNull();
        assertThat(payment.getStatus()).isNull();
    }
}
