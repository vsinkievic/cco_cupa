package lt.creditco.cupa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.PaymentCurrency;
import lt.creditco.cupa.remote.PaymentReply;
import lt.creditco.cupa.repository.ClientRepository;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import lt.creditco.cupa.service.mapper.PaymentTransactionMapper;
import lt.creditco.cupa.web.context.CupaApiContext;
import lt.creditco.cupa.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    private PaymentTransactionDTO validPaymentTransactionDTO;
    private PaymentTransaction validPaymentTransaction;
    private PaymentRequest validPaymentRequest;
    private CupaApiContext.CupaApiContextData validContext;
    private Client client;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        validPaymentTransactionDTO = new PaymentTransactionDTO();
        validPaymentTransactionDTO.setId("test-id");
        validPaymentTransactionDTO.setOrderId("test-order-123");
        validPaymentTransactionDTO.setClientId("CLN-00001");
        validPaymentTransactionDTO.setMerchantId("MERCH-00001");
        validPaymentTransactionDTO.setAmount(new BigDecimal("100.00"));
        validPaymentTransactionDTO.setCurrency(Currency.USD);
        validPaymentTransactionDTO.setPaymentBrand(PaymentBrand.UnionPay);
        validPaymentTransactionDTO.setStatus(TransactionStatus.RECEIVED);
        validPaymentTransactionDTO.setRequestTimestamp(Instant.now());

        validPaymentTransaction = new PaymentTransaction();
        validPaymentTransaction.setId("test-id");
        validPaymentTransaction.setOrderId("test-order-123");
        validPaymentTransaction.setClientId("CLN-00001");
        validPaymentTransaction.setMerchantId("MERCH-00001");
        validPaymentTransaction.setAmount(new BigDecimal("100.00"));
        validPaymentTransaction.setCurrency(Currency.USD);
        validPaymentTransaction.setPaymentBrand(PaymentBrand.UnionPay);
        validPaymentTransaction.setStatus(TransactionStatus.RECEIVED);
        validPaymentTransaction.setRequestTimestamp(Instant.now());

        validPaymentRequest = new PaymentRequest();
        validPaymentRequest.setOrderId("test-order-123");
        validPaymentRequest.setClientId("CLN-00001");
        validPaymentRequest.setAmount(new BigDecimal("100.00"));
        validPaymentRequest.setCurrency(PaymentCurrency.USD);
        validPaymentRequest.setCardType(CardType.UnionPay);

        validContext = CupaApiContext.CupaApiContextData.builder().merchantId("MERCH-00001")/* .environment("TEST")*/.build();

        // Setup test data for enrichment tests
        client = new Client();
        client.setId("test-client-id");
        client.setMerchantClientId("merchant-client-id");
        client.setName("Test Client Name");

        merchant = new Merchant();
        merchant.setId("test-merchant-id");
        merchant.setName("Test Merchant Name");
    }

    @Test
    void shouldSaveValidPaymentTransaction() {
        // Given
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);
        when(paymentTransactionMapper.toEntity(validPaymentTransactionDTO)).thenReturn(validPaymentTransaction);
        when(paymentTransactionRepository.save(validPaymentTransaction)).thenReturn(validPaymentTransaction);
        when(paymentTransactionMapper.toDto(validPaymentTransaction)).thenReturn(validPaymentTransactionDTO);

        // When
        PaymentTransactionDTO result = paymentTransactionService.save(validPaymentTransactionDTO, validContext);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-id");
    }

    @Test
    void shouldThrowExceptionWhenClientNotFound() {
        // Given
        when(clientRepository.existsById("CLN-00001")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Client with ID=CLN-00001 not found!");
    }

    @Test
    void shouldThrowExceptionWhenMerchantNotFound() {
        // Given
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Merchant with ID=MERCH-00001 not found!");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        // Given
        validPaymentTransactionDTO.setAmount(BigDecimal.ZERO);
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        // Given
        validPaymentTransactionDTO.setAmount(new BigDecimal("-10.00"));
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        // Given
        validPaymentTransactionDTO.setCurrency(null);
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Currency is required");
    }

    @Test
    void shouldThrowExceptionWhenPaymentBrandIsNull() {
        // Given
        validPaymentTransactionDTO.setPaymentBrand(null);
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Payment brand is required");
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        // Given
        validPaymentTransactionDTO.setOrderId(null);
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Order ID is required");
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsEmpty() {
        // Given
        validPaymentTransactionDTO.setOrderId("");
        when(clientRepository.existsById("CLN-00001")).thenReturn(true);
        when(merchantRepository.existsById("MERCH-00001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentTransactionService.save(validPaymentTransactionDTO, validContext))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Order ID is required");
    }

    @Test
    void shouldEnrichDTOWhenFindingOne() {
        // Given
        PaymentTransactionDTO testDTO = new PaymentTransactionDTO();
        testDTO.setId("test-id");
        testDTO.setClientId("test-client-id");
        testDTO.setMerchantId("test-merchant-id");

        when(paymentTransactionRepository.findOneWithEagerRelationships("test-id")).thenReturn(Optional.of(validPaymentTransaction));
        when(paymentTransactionMapper.toDto(validPaymentTransaction)).thenReturn(testDTO);
        when(clientRepository.findById("test-client-id")).thenReturn(Optional.of(client));
        when(merchantRepository.findById("test-merchant-id")).thenReturn(Optional.of(merchant));

        // When
        Optional<PaymentTransactionDTO> result = paymentTransactionService.findOne("test-id");

        // Then
        assertThat(result).isPresent();
        PaymentTransactionDTO enrichedDTO = result.orElse(null);
        assertThat(enrichedDTO.getMerchantClientId()).isEqualTo("merchant-client-id");
        assertThat(enrichedDTO.getClientName()).isEqualTo("Test Client Name");
        assertThat(enrichedDTO.getMerchantName()).isEqualTo("Test Merchant Name");
    }

    @Test
    void testGetTransactionStatusFromReply_Result0_ShouldReturnSuccess() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("0");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.SUCCESS, result);
    }

    @Test
    void testGetTransactionStatusFromReply_Result1_ShouldReturnPending() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("1");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.PENDING, result);
    }

    @Test
    void testGetTransactionStatusFromReply_Result11_ShouldReturnAbandoned() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("11");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.ABANDONED, result);
    }

    @Test
    void testGetTransactionStatusFromReply_OtherResultWithSuccessY_ShouldReturnSuccess() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("5");
        paymentReply.setSuccess("Y");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.SUCCESS, result);
    }

    @Test
    void testGetTransactionStatusFromReply_OtherResultWithSuccessN_ShouldReturnFailed() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("5");
        paymentReply.setSuccess("N");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.FAILED, result);
    }

    @Test
    void testGetTransactionStatusFromReply_Result0WithSuccessN_ShouldReturnSuccess() {
        // Given - Result "0" should take precedence over success field
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("0");
        paymentReply.setSuccess("N");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.SUCCESS, result);
    }

    @Test
    void testGetTransactionStatusFromReply_Result1WithSuccessY_ShouldReturnPending() {
        // Given - Result "1" should take precedence over success field
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("1");
        paymentReply.setSuccess("Y");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.PENDING, result);
    }

    @Test
    void testGetTransactionStatusFromReply_Result11WithSuccessY_ShouldReturnAbandoned() {
        // Given - Result "11" should take precedence over success field
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("11");
        paymentReply.setSuccess("Y");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.ABANDONED, result);
    }

    @Test
    void testGetTransactionStatusFromReply_OtherResultWithSuccessNull_ShouldReturnNull() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("5");
        paymentReply.setSuccess(null);

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertNull(result);
    }

    @Test
    void testGetTransactionStatusFromReply_ResultNullWithSuccessY_ShouldReturnSuccess() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult(null);
        paymentReply.setSuccess("Y");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.SUCCESS, result);
    }

    @Test
    void testGetTransactionStatusFromReply_ResultNullWithSuccessN_ShouldReturnFailed() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult(null);
        paymentReply.setSuccess("N");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertEquals(TransactionStatus.FAILED, result);
    }

    @Test
    void testGetTransactionStatusFromReply_ResultNullWithSuccessNull_ShouldReturnNull() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult(null);
        paymentReply.setSuccess(null);

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertNull(result);
    }

    @Test
    void testGetTransactionStatusFromReply_NullPaymentReply_ShouldReturnNull() {
        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(null);

        // Then
        assertNull(result);
    }

    @Test
    void testGetTransactionStatusFromReply_OtherResultWithSuccessInvalid_ShouldReturnNull() {
        // Given
        PaymentReply paymentReply = new PaymentReply();
        paymentReply.setResult("5");
        paymentReply.setSuccess("INVALID");

        // When
        TransactionStatus result = paymentTransactionService.getTransactionStatusFromReply(paymentReply);

        // Then
        assertNull(result);
    }
}
