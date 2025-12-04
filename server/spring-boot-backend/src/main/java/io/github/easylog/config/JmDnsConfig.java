package io.github.easylog.config;

import io.github.easylog.service.JmDnsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Peter Szrnka
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JmDnsConfig {

    private final JmDnsService jmDnsService;

    @PostConstruct
    public void init() throws IOException {
        jmDnsService.init();
    }

    @PreDestroy
    public void onShutdown() {
        jmDnsService.onShutdown();
    }
}
