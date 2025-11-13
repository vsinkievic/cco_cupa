package lt.creditco.cupa.config;

import lombok.RequiredArgsConstructor;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.web.filter.HttpLoggingFilter;
import lt.creditco.cupa.web.interceptor.CupaApiAuditInterceptor;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for registering interceptors.
 */
@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)  // Ensure this runs first
public class WebMvcConfig implements WebMvcConfigurer {

    private final CupaApiAuditInterceptor auditInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor).addPathPatterns("/api/**");
    }


    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter(AuditLogService auditLogService) {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpLoggingFilter(auditLogService));
        registrationBean.addUrlPatterns("/api/*", "/api/v1/*", "/public/webhook/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.setName("httpLoggingFilter");
        return registrationBean;
    }

}
