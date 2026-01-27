package lt.creditco.cupa.service.tasks;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bpmid.pulltasks.application.TaskHandler;
import com.bpmid.pulltasks.domain.PullTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.web.context.CupaApiContext;

/**
 * Task handler for querying payment status from the remote gateway.
 * 
 * <p>This task is automatically scheduled when a payment transaction enters PENDING status.
 * It queries the gateway at progressively increasing intervals:
 * <ul>
 *   <li>0-1 hour: every 1 minute</li>
 *   <li>1-3 hours: every 10 minutes</li>
 *   <li>3+ hours: every 60 minutes</li>
 * </ul>
 * 
 * <p>After 24 hours without a final status, the transaction is marked as ABANDONED.
 */
@Slf4j
@Component(QueryPaymentStatusTask.TASK_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class QueryPaymentStatusTask implements TaskHandler {

    public static final String TASK_NAME = "query-payment-status";
    
    /** Timeout period after which pending transactions are marked as ABANDONED */
    public static final int TIMEOUT_PERIOD_HOURS = 24;
    
    /** Polling interval in first hour (seconds) */
    private static final int INTERVAL_FIRST_HOUR_SECONDS = 60;
    
    /** Polling interval between 1-3 hours (seconds) */
    private static final int INTERVAL_1_TO_3_HOURS_SECONDS = 600;
    
    /** Polling interval after 3 hours (seconds) */
    private static final int INTERVAL_AFTER_3_HOURS_SECONDS = 3600;
    
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MerchantRepository merchantRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final ObjectMapper objectMapper;

    /**
     * Task payload containing the transaction ID to query.
     */
    public record TaskPayload(String transactionId) {}

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Integer getMaxAttempts() {
        return 0; // Infinite retries - handled by timeout logic
    }

    @Override
    @Transactional
    public void execute(PullTask task) throws Exception {
        log.debug("Executing QueryPaymentStatusTask: businessKey={}, id={}", 
            task.getBusinessKey(), task.getId());
        
        // Parse payload
        TaskPayload payload = parsePayload(task);
        String transactionId = payload.transactionId();
        
        // Load transaction
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalStateException(
                "PaymentTransaction not found: " + transactionId));
        
        // Check if still pending
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.info("Transaction {} is no longer PENDING (status: {}), completing task", 
                transactionId, transaction.getStatus());
            return; // Task completes, no reschedule
        }
        
        // Calculate elapsed time
        Instant requestTime = transaction.getRequestTimestamp();
        if (requestTime == null) {
            requestTime = transaction.getCreatedDate();
        }
        Duration elapsed = Duration.between(requestTime, Instant.now());
        
        // Check for timeout
        if (elapsed.toHours() >= TIMEOUT_PERIOD_HOURS) {
            log.warn("Transaction {} timed out after {} hours, marking as ABANDONED", 
                transactionId, elapsed.toHours());
            transaction.setStatus(TransactionStatus.ABANDONED);
            transaction.setStatusDescription("Timed out after " + TIMEOUT_PERIOD_HOURS + " hours without final status");
            paymentTransactionRepository.saveAndFlush(transaction);
            task.fail("Transaction timed out after " + TIMEOUT_PERIOD_HOURS + " hours", null);
            return;
        }
        
        // Load merchant and build context
        CupaApiContext.CupaApiContextData context = buildApiContext(transaction);
        
        // Query gateway
        log.info("Querying gateway for transaction: {}, orderId: {}, elapsed: {} minutes", 
            transactionId, transaction.getOrderId(), elapsed.toMinutes());
        
        var updatedTransaction = paymentTransactionService.queryPaymentFromGateway(transactionId, context);
        
        // Check if still pending using the returned DTO
        if (updatedTransaction.getStatus() == TransactionStatus.PENDING) {
            // Schedule next query with appropriate interval
            int intervalSeconds = calculateNextInterval(elapsed);
            task.addUniqueResultTask(task.cloneAndRescheduleAt(Instant.now().plusSeconds(intervalSeconds)));
        } else {
            log.info("Transaction {} now has final status: {}", 
                transactionId, updatedTransaction.getStatus());
            // Task completes naturally
        }
    }
    
    private TaskPayload parsePayload(PullTask task) throws JsonProcessingException {
        if (task.getPayload() == null || task.getPayload().isBlank()) {
            throw new IllegalStateException("Task payload is empty");
        }
        return objectMapper.readValue(task.getPayload(), TaskPayload.class);
    }
    
    private CupaApiContext.CupaApiContextData buildApiContext(PaymentTransaction transaction) {
        String merchantId = transaction.getMerchantId();
        if (merchantId == null) {
            throw new IllegalStateException(
                "Transaction " + transaction.getId() + " has no merchantId");
        }
        
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new IllegalStateException(
                "Merchant not found: " + merchantId));
        
        if (!MerchantStatus.ACTIVE.equals(merchant.getStatus())) {
            throw new IllegalStateException(
                "Merchant " + merchantId + " is not active (status: " + merchant.getStatus() + ")");
        }
        
        // Determine mode from transaction environment, fallback to merchant mode
        MerchantMode mode = transaction.getEnvironment();
        if (mode == null) {
            mode = merchant.getMode();
        }
        
        // Build merchant context based on mode
        CupaApiContext.MerchantContext merchantContext = buildMerchantContext(merchant, mode);
        
        return CupaApiContext.CupaApiContextData.builder()
            .merchantId(merchantId)
            .merchantContext(merchantContext)
            .requestTimestamp(Instant.now())
            .build();
    }
    
    private CupaApiContext.MerchantContext buildMerchantContext(Merchant merchant, MerchantMode mode) {
        String gatewayUrl;
        String gatewayMerchantId;
        String gatewayMerchantKey;
        String gatewayApiKey;
        
        if (mode == MerchantMode.LIVE) {
            gatewayUrl = merchant.getRemoteProdUrl();
            gatewayMerchantId = merchant.getRemoteProdMerchantId();
            gatewayMerchantKey = merchant.getRemoteProdMerchantKey();
            gatewayApiKey = merchant.getRemoteProdApiKey();
        } else {
            gatewayUrl = merchant.getRemoteTestUrl();
            gatewayMerchantId = merchant.getRemoteTestMerchantId();
            gatewayMerchantKey = merchant.getRemoteTestMerchantKey();
            gatewayApiKey = merchant.getRemoteTestApiKey();
        }
        
        // Validate required credentials
        if (gatewayUrl == null || gatewayUrl.isBlank()) {
            throw new IllegalStateException(
                "Merchant " + merchant.getId() + " has no gateway URL configured for mode " + mode);
        }
        if (gatewayMerchantId == null || gatewayMerchantId.isBlank()) {
            throw new IllegalStateException(
                "Merchant " + merchant.getId() + " has no gateway merchant ID configured for mode " + mode);
        }
        if (gatewayMerchantKey == null || gatewayMerchantKey.isBlank()) {
            throw new IllegalStateException(
                "Merchant " + merchant.getId() + " has no gateway merchant key configured for mode " + mode);
        }
        
        return CupaApiContext.MerchantContext.builder()
            .merchantId(merchant.getId())
            .mode(mode)
            .status(merchant.getStatus())
            .gatewayUrl(gatewayUrl)
            .gatewayMerchantId(gatewayMerchantId)
            .gatewayMerchantKey(gatewayMerchantKey)
            .gatewayApiKey(gatewayApiKey)
            .build();
    }
    
    private int calculateNextInterval(Duration elapsed) {
        long elapsedHours = elapsed.toHours();
        
        if (elapsedHours < 1) {
            return INTERVAL_FIRST_HOUR_SECONDS;
        } else if (elapsedHours < 3) {
            return INTERVAL_1_TO_3_HOURS_SECONDS;
        } else {
            return INTERVAL_AFTER_3_HOURS_SECONDS;
        }
    }
    
}
