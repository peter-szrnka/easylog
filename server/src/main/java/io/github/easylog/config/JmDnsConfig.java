package io.github.easylog.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JmDnsConfig {

    @Value("${config.easylog.service.name}")
    private String serviceName;
    @Value("${server.port}")
    private int port;
    @Value("${config.easylog.service.type}")
    private String serviceType;
    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;
    private final Environment environment;

    private JmDNS jmdns;

    @PostConstruct
    public void init() throws IOException {
        InetAddress localAddress = InetAddress.getLocalHost();
        log.info("Using interface: {} / {}", localAddress.getHostName(), localAddress.getHostAddress());

        jmdns = JmDNS.create(localAddress);
        ServiceInfo serviceInfo = createServiceInfo();

        jmdns.registerService(serviceInfo);
        log.info("Service registered: {} on {}", serviceInfo.getName(), localAddress.getHostAddress());
    }

    @PreDestroy
    public void onShutdown() {
        try {
            if (jmdns != null) {
                log.info("Unregistering service...");
                jmdns.unregisterAllServices();
                jmdns.close();
            }
        } catch (Exception e) {
            log.error("Error while closing JmDNS", e);
        }
    }

    private ServiceInfo createServiceInfo() {
        Map<String, String> properties = Map.of(
                "path", "/api/log",
                "ssl", String.valueOf(sslEnabled)
        );

        return ServiceInfo.create(serviceType, serviceName, port, 0, 0, properties);
    }
}
