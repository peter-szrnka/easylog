package io.github.easylog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.service.JmDnsService;
import io.github.easylog.service.LogService;
import io.github.easylog.service.WebsocketMessagingClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Peter Szrnka
 */
@Configuration
public class ApplicationConfig {

    @Value("${config.easylog.service.name}")
    private String serviceName;
    @Value("${server.port}")
    private int port;
    @Value("${config.easylog.service.type}")
    private String serviceType;
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public LogService logService(WebsocketMessagingClientService websocketMessagingClientService, LogEntityDao logEntityDao) {
        return new LogService(websocketMessagingClientService, logEntityDao);
    }

    @Bean
    public JmDnsService jmDnsService() {
        return new JmDnsService(serviceName, port, serviceType, sslEnabled);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}
