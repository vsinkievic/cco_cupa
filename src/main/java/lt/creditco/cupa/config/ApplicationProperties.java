package lt.creditco.cupa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Properties specific to Cupa.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * Extends vapp-base ApplicationProperties to inherit common properties (name, shortName, teamName).
 * Marked as @Primary to take precedence over the parent bean when both are registered.
 */
@Primary
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties extends com.bpmid.vapp.config.ApplicationProperties {


}
