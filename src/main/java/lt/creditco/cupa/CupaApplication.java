package lt.creditco.cupa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.bpmid.vapp.AbstractVApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

import lt.creditco.cupa.config.ApplicationProperties;
import tech.jhipster.config.DefaultProfileUtil;

/**
 * Main Spring Boot application for CUPA (Creditco UnionPay Acquiring).
 *
 * <p>This application integrates:
 * <ul>
 *   <li><strong>vapp-base</strong>: Provides base UI framework, user management, security</li>
 *   <li><strong>JHipster framework</strong>: Legacy backend infrastructure (gradually being phased out)</li>
 *   <li><strong>Vaadin</strong>: Modern UI framework for admin and merchant portals</li>
 * </ul>
 *
 * <p>The application serves both Vaadin UI and REST API endpoints:
 * <ul>
 *   <li><strong>/ui/**</strong>: Vaadin-based UI (session-based authentication from vapp-base)</li>
 *   <li><strong>/api/v1/**</strong>: Merchant API (X-API-Key authentication via CupaApiSecurityConfiguration)</li>
 *   <li><strong>/api/**</strong>: Future API versions (X-API-Key authentication)</li>
 *   <li><strong>/public/webhook</strong>: Public webhook for external integrations</li>
 * </ul>
 *
 * <p>Security Architecture:
 * <ul>
 *   <li>API security: CupaApiSecurityConfiguration extends vapp-base ApiSecurityConfiguration, overrides to use X-API-Key</li>
 *   <li>UI security: Uses vapp-base VaadinSecurityConfiguration (session-based)</li>
 * </ul>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "lt.creditco.cupa",           // CUPA application components
    "com.bpmid"                   // vapp-base components
}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.bpmid.vapp.config.CacheConfiguration.class
        // Security configs: CupaApiSecurityConfiguration extends and replaces vapp-base ApiSecurityConfiguration
        // via @ConditionalOnMissingBean. VaadinSecurityConfiguration from vapp-base is used as-is.
        // ApplicationProperties: cco_cupa extends vapp-base version and is marked as @Primary
    })
})
@EnableJpaRepositories(basePackages = {"lt.creditco.cupa", "com.bpmid"})
@EntityScan(basePackages = {"lt.creditco.cupa", "com.bpmid"})
@Theme(value = "vapp-theme")
@PWA(
    name = "CUPA - Creditco UnionPay Acquiring",
    shortName = "CUPA",
    offlinePath = "offline.html",
    offlineResources = { "images/offline.png" }
)
@Import(AbstractVApplication.class)
@EnableConfigurationProperties({ LiquibaseProperties.class, ApplicationProperties.class })
public class CupaApplication extends AbstractVApplication implements AppShellConfigurator {
    
    public CupaApplication(Environment env) {
        super(env);
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CupaApplication.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

}

