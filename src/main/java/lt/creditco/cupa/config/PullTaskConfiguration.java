package lt.creditco.cupa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import com.bpmid.pulltasks.application.DefaultTaskLeasingAgent;
import com.bpmid.pulltasks.application.PullTaskService;
import com.bpmid.pulltasks.application.TaskLeasingAgentConfig;
import com.bpmid.pulltasks.config.DynamicTaskHandlerProviderRegistry;
import com.bpmid.pulltasks.config.PullTasksProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Pull-Tasks queue processing in CUPA.
 * Configures a single task leasing agent for the default pool.
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "pulltasks", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PullTaskConfiguration {
    
    public static final String DEFAULT_POOL = "default";

    private final DefaultTaskLeasingAgent taskAgent;

    public PullTaskConfiguration(PullTaskService pullTaskService,
            DynamicTaskHandlerProviderRegistry handlerRegistry,
            PullTasksProperties properties,
            TransactionTemplate transactionTemplate) {

        var allProviders = handlerRegistry.getAllProviders();

        TaskLeasingAgentConfig agentConfig = TaskLeasingAgentConfig.builder()
            .agentName("cupa-task-agent")
            .owner(properties.getOwner())
            .poolName(DEFAULT_POOL)
            .maxWorkers(10)
            .leaseTimeInSeconds(600)
            .build();
        
        this.taskAgent = new DefaultTaskLeasingAgent(agentConfig, allProviders, pullTaskService, transactionTemplate);
        
        log.info("PullTaskConfiguration initialized with pool: {}, owner: {}", DEFAULT_POOL, properties.getOwner());
    }

    /**
     * Polls for and executes due tasks every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void processTasks() {
        taskAgent.leaseAndExecuteTasks();
    }
}
