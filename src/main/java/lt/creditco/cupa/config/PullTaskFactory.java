package lt.creditco.cupa.config;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.bpmid.pulltasks.config.PullTasksProperties;
import com.bpmid.pulltasks.domain.PullTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.service.tasks.QueryPaymentStatusTask;

/**
 * Factory for creating PullTask instances with predefined configurations.
 * Encapsulates task creation logic to ensure consistent pool and owner settings.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "pulltasks", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PullTaskFactory {
    
    private final PullTasksProperties properties;
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a new task configured to run in the default pool.
     * 
     * @param taskName The name of the task (must match a TaskHandler's getTaskName())
     * @param businessKey Unique business identifier for the task
     * @return A new PullTask configured for the default pool
     */
    public PullTask newTask(String taskName, String businessKey) {
        return new PullTask(properties.getOwner(), taskName, businessKey)
            .executorPool(PullTaskConfiguration.DEFAULT_POOL);
    }
    
    /**
     * Creates a QueryPaymentStatusTask for a pending payment transaction.
     * Uses orderId as businessKey for business-meaningful tracking.
     * First query is scheduled 1 minute from now.
     * 
     * @param paymentTransaction the pending payment transaction
     * @return configured PullTask ready for enqueueing
     */
    public PullTask createQueryPaymentStatusTask(PaymentTransaction paymentTransaction) {
        PullTask task = new PullTask(properties.getOwner(), 
            QueryPaymentStatusTask.TASK_NAME, 
            paymentTransaction.getOrderId())  // orderId as businessKey
            .executorPool(PullTaskConfiguration.DEFAULT_POOL);
        
        try {
            task.setPayload(objectMapper.writeValueAsString(
                new QueryPaymentStatusTask.TaskPayload(paymentTransaction.getId())
            ));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize task payload for transaction: {}", paymentTransaction.getId(), e);
            throw new RuntimeException("Failed to create QueryPaymentStatusTask payload", e);
        }
        
        task.setDueDate(Instant.now().plusSeconds(60)); // First query in 1 minute
        
        log.debug("Created QueryPaymentStatusTask for transaction: {}, orderId: {}", 
            paymentTransaction.getId(), paymentTransaction.getOrderId());
        
        return task;
    }
}
