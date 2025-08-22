package lt.creditco.cupa.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceUpdateEventListenerTest {

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    private BalanceUpdateEventListener eventListener;

    private PaymentTransaction paymentTransaction;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        eventListener = new BalanceUpdateEventListener(paymentTransactionService, paymentTransactionRepository, merchantRepository);

        // Create test payment transaction
        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setId("test-transaction-id");
        paymentTransaction.setMerchantId("test-merchant-id");
        paymentTransaction.setOrderId("test-order-id");
        paymentTransaction.setAmount(new BigDecimal("100.00"));
        paymentTransaction.setCurrency(Currency.USD);
        paymentTransaction.setStatus(TransactionStatus.PENDING);

        // Create test merchant
        merchant = new Merchant();
        merchant.setId("test-merchant-id");
        merchant.setName("Test Merchant");
        merchant.setMode(MerchantMode.TEST);
        merchant.setStatus(MerchantStatus.ACTIVE);
        merchant.setCupaTestApiKey("test-api-key");
        merchant.setRemoteTestUrl("https://test-gateway.com");
        merchant.setRemoteTestMerchantId("test-gateway-merchant-id");
        merchant.setRemoteTestMerchantKey("test-gateway-merchant-key");
        merchant.setRemoteTestApiKey("test-gateway-api-key");
    }

    @Test
    void shouldHandleBalanceUpdateEvent() {
        // Given
        BalanceUpdateEvent event = new BalanceUpdateEvent(this, "test-transaction-id", "test-merchant-id", "test-order-id");

        when(paymentTransactionRepository.findById("test-transaction-id")).thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id")).thenReturn(Optional.of(merchant));

        // When
        eventListener.handleBalanceUpdateEvent(event);

        // Then
        verify(paymentTransactionService).queryPaymentFromGateway(eq("test-transaction-id"), any());
    }

    @Test
    void shouldHandleEventWhenPaymentTransactionNotFound() {
        // Given
        BalanceUpdateEvent event = new BalanceUpdateEvent(this, "non-existent-transaction-id", "test-merchant-id", "test-order-id");

        when(paymentTransactionRepository.findById("non-existent-transaction-id")).thenReturn(Optional.empty());

        // When
        eventListener.handleBalanceUpdateEvent(event);

        // Then
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    void shouldHandleEventWhenMerchantNotFound() {
        // Given
        BalanceUpdateEvent event = new BalanceUpdateEvent(this, "test-transaction-id", "non-existent-merchant-id", "test-order-id");

        when(paymentTransactionRepository.findById("test-transaction-id")).thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("non-existent-merchant-id")).thenReturn(Optional.empty());

        // When
        eventListener.handleBalanceUpdateEvent(event);

        // Then
        verifyNoInteractions(paymentTransactionService);
    }
}
