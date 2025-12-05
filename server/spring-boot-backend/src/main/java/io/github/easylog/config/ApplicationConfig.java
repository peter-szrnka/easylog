package io.github.easylog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.service.JmDnsService;
import io.github.easylog.service.LogService;
import io.github.easylog.service.WebsocketMessagingClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * @author Peter Szrnka
 */
@Slf4j
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    @Value("${config.easylog.service.name}")
    private String serviceName;
    @Value("${server.port}")
    private int port;
    @Value("${config.easylog.service.type}")
    private String serviceType;
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;
    @Value("${config.easylog.disable-cors:false}")
    private boolean disableCors;

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

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (!disableCors) {
            return;
        }

        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
                .allowedMethods("*")
                .allowCredentials(true);
        log.info("CORS configuration disabled in development profile");
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        return checkRequestedResource(requestedResource) ? requestedResource : new ClassPathResource("/static/index.html");
                    }
                });
    }

    private static boolean checkRequestedResource(Resource requestedResource) {
        return requestedResource.exists() && requestedResource.isReadable();
    }
}
