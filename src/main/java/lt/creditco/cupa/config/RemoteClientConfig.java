package lt.creditco.cupa.config;

import lt.creditco.cupa.remote.NoOpResponseErrorHandler;
import lt.creditco.cupa.remote.RestTemplateBodyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RemoteClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBodyInterceptor restTemplateBodyInterceptor) {
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(restTemplateBodyInterceptor);
        restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
        return restTemplate;
    }
}
