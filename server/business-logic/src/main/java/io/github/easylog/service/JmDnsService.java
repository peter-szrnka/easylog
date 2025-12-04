package io.github.easylog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * @author Peter Szrnka
 */
@Slf4j
@RequiredArgsConstructor
public class JmDnsService {

    private final String serviceName;
    private final int port;
    private final String serviceType;
    private final boolean sslEnabled;

    private JmDNS jmdns;

    public void init() throws IOException {
        InetAddress localAddress = InetAddress.getLocalHost();
        log.info("Using interface: {} / {}", localAddress.getHostName(), localAddress.getHostAddress());

        jmdns = JmDNS.create(localAddress);
        ServiceInfo serviceInfo = createServiceInfo();

        jmdns.registerService(serviceInfo);
        log.info("Service registered: {} on {}", serviceInfo.getName(), localAddress.getHostAddress());
    }

    public void onShutdown() {
        if (jmdns == null) {
            return;
        }

        try {
            log.info("Unregistering service...");
            jmdns.unregisterAllServices();
            jmdns.close();
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
