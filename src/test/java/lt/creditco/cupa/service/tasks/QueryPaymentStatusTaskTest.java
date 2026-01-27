package lt.creditco.cupa.service.tasks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bpmid.pulltasks.domain.PullTask;
import com.bpmid.pulltasks.domain.PullTaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;

@ExtendWith(MockitoExtension.class)
class QueryPaymentStatusTaskTest {

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    private ObjectMapper objectMapper;
    private QueryPaymentStatusTask task;

    private PaymentTransaction paymentTransaction;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        task = new QueryPaymentStatusTask(
            paymentTransactionRepository,
            merchantRepository,
            paymentTransactionService,
            objectMapper
        );

        // Create test payment transaction
        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setId("test-transaction-id");
        paymentTransaction.setMerchantId("test-merchant-id");
        paymentTransaction.setOrderId("test-order-id");
        paymentTransaction.setAmount(new BigDecimal("100.00"));
        paymentTransaction.setCurrency(Currency.USD);
        paymentTransaction.setPaymentBrand(PaymentBrand.UnionPay);
        paymentTransaction.setStatus(TransactionStatus.PENDING);
        paymentTransaction.setRequestTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES));
        paymentTransaction.setEnvironment(MerchantMode.TEST);

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
    void shouldReturnCorrectTaskName() {
        assertEquals("query-payment-status", task.getTaskName());
    }

    @Test
    void shouldCompleteTaskWhenTransactionNotPending() throws Exception {
        // Given - transaction already succeeded
        paymentTransaction.setStatus(TransactionStatus.SUCCESS);
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));

        // When
        task.execute(pullTask);

        // Then - no gateway query, no reschedule
        verifyNoInteractions(paymentTransactionService);
        assertTrue(pullTask.getUniqueResultTasks().isEmpty());
    }

    @Test
    void shouldQueryGatewayForPendingTransaction() throws Exception {
        // Given
        PullTask pullTask = createPullTask();
        
        // Transaction still pending after query
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));
        
        // Return DTO with PENDING status (still pending after query)
        PaymentTransactionDTO pendingDto = new PaymentTransactionDTO();
        pendingDto.setStatus(TransactionStatus.PENDING);
        when(paymentTransactionService.queryPaymentFromGateway(eq("test-transaction-id"), any()))
            .thenReturn(pendingDto);

        // When
        task.execute(pullTask);

        // Then - should query gateway
        verify(paymentTransactionService).queryPaymentFromGateway(eq("test-transaction-id"), any());
        
        // Should schedule next query (within first hour, so 60 seconds)
        assertEquals(1, pullTask.getUniqueResultTasks().size());
        PullTask nextTask = pullTask.getUniqueResultTasks().get(0);
        assertNotNull(nextTask.getDueDate());
        assertTrue(nextTask.getDueDate().isAfter(Instant.now()));
    }

    @Test
    void shouldNotRescheduleWhenTransactionCompletes() throws Exception {
        // Given
        PullTask pullTask = createPullTask();
        
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));
        
        // Return DTO with SUCCESS status (transaction completed after query)
        PaymentTransactionDTO successDto = new PaymentTransactionDTO();
        successDto.setStatus(TransactionStatus.SUCCESS);
        when(paymentTransactionService.queryPaymentFromGateway(eq("test-transaction-id"), any()))
            .thenReturn(successDto);

        // When
        task.execute(pullTask);

        // Then - should query gateway but not reschedule
        verify(paymentTransactionService).queryPaymentFromGateway(eq("test-transaction-id"), any());
        assertTrue(pullTask.getUniqueResultTasks().isEmpty());
    }

    @Test
    void shouldMarkAsAbandonedAfterTimeout() throws Exception {
        // Given - transaction older than 24 hours
        paymentTransaction.setRequestTimestamp(Instant.now().minus(25, ChronoUnit.HOURS));
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));

        // When
        task.execute(pullTask);

        // Then - should mark as abandoned
        verify(paymentTransactionRepository).saveAndFlush(paymentTransaction);
        assertEquals(TransactionStatus.ABANDONED, paymentTransaction.getStatus());
        assertTrue(paymentTransaction.getStatusDescription().contains("Timed out"));
        
        // Should fail the task
        assertEquals(PullTaskStatus.FAILED, pullTask.getStatus());
        
        // No gateway query, no reschedule
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalStateException.class, () -> task.execute(pullTask));
    }

    @Test
    void shouldThrowExceptionWhenMerchantNotFound() {
        // Given
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalStateException.class, () -> task.execute(pullTask));
    }

    @Test
    void shouldThrowExceptionWhenMerchantNotActive() {
        // Given
        merchant.setStatus(MerchantStatus.INACTIVE);
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> task.execute(pullTask));
        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    void shouldThrowExceptionWhenGatewayCredentialsMissing() {
        // Given - missing gateway URL
        merchant.setRemoteTestUrl(null);
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> task.execute(pullTask));
        assertTrue(exception.getMessage().contains("gateway URL"));
    }

    @Test
    void shouldUseCorrectPollingIntervalForFirstHour() throws Exception {
        // Given - 30 minutes elapsed
        paymentTransaction.setRequestTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES));
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));
        
        // Return DTO with PENDING status
        PaymentTransactionDTO pendingDto = new PaymentTransactionDTO();
        pendingDto.setStatus(TransactionStatus.PENDING);
        when(paymentTransactionService.queryPaymentFromGateway(eq("test-transaction-id"), any()))
            .thenReturn(pendingDto);

        // When
        task.execute(pullTask);

        // Then - should reschedule in ~60 seconds
        assertEquals(1, pullTask.getUniqueResultTasks().size());
        PullTask nextTask = pullTask.getUniqueResultTasks().get(0);
        long secondsUntilNext = java.time.Duration.between(Instant.now(), nextTask.getDueDate()).toSeconds();
        assertTrue(secondsUntilNext >= 55 && secondsUntilNext <= 65, 
            "Expected ~60 seconds, got: " + secondsUntilNext);
    }

    @Test
    void shouldUseCorrectPollingIntervalBetween1And3Hours() throws Exception {
        // Given - 2 hours elapsed
        paymentTransaction.setRequestTimestamp(Instant.now().minus(2, ChronoUnit.HOURS));
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));
        
        // Return DTO with PENDING status
        PaymentTransactionDTO pendingDto = new PaymentTransactionDTO();
        pendingDto.setStatus(TransactionStatus.PENDING);
        when(paymentTransactionService.queryPaymentFromGateway(eq("test-transaction-id"), any()))
            .thenReturn(pendingDto);

        // When
        task.execute(pullTask);

        // Then - should reschedule in ~600 seconds (10 minutes)
        assertEquals(1, pullTask.getUniqueResultTasks().size());
        PullTask nextTask = pullTask.getUniqueResultTasks().get(0);
        long secondsUntilNext = java.time.Duration.between(Instant.now(), nextTask.getDueDate()).toSeconds();
        assertTrue(secondsUntilNext >= 595 && secondsUntilNext <= 605, 
            "Expected ~600 seconds, got: " + secondsUntilNext);
    }

    @Test
    void shouldUseCorrectPollingIntervalAfter3Hours() throws Exception {
        // Given - 4 hours elapsed
        paymentTransaction.setRequestTimestamp(Instant.now().minus(4, ChronoUnit.HOURS));
        
        PullTask pullTask = createPullTask();
        when(paymentTransactionRepository.findById("test-transaction-id"))
            .thenReturn(Optional.of(paymentTransaction));
        when(merchantRepository.findById("test-merchant-id"))
            .thenReturn(Optional.of(merchant));
        
        // Return DTO with PENDING status
        PaymentTransactionDTO pendingDto = new PaymentTransactionDTO();
        pendingDto.setStatus(TransactionStatus.PENDING);
        when(paymentTransactionService.queryPaymentFromGateway(eq("test-transaction-id"), any()))
            .thenReturn(pendingDto);

        // When
        task.execute(pullTask);

        // Then - should reschedule in ~3600 seconds (60 minutes)
        assertEquals(1, pullTask.getUniqueResultTasks().size());
        PullTask nextTask = pullTask.getUniqueResultTasks().get(0);
        long secondsUntilNext = java.time.Duration.between(Instant.now(), nextTask.getDueDate()).toSeconds();
        assertTrue(secondsUntilNext >= 3595 && secondsUntilNext <= 3605, 
            "Expected ~3600 seconds, got: " + secondsUntilNext);
    }

    private PullTask createPullTask() {
        try {
            PullTask pullTask = new PullTask("cupa", QueryPaymentStatusTask.TASK_NAME, "test-order-id");
            pullTask.setPayload(objectMapper.writeValueAsString(
                new QueryPaymentStatusTask.TaskPayload("test-transaction-id")
            ));
            return pullTask;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
