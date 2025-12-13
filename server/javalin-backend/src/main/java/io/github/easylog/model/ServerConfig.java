package io.github.easylog.model;

/**
 * @author Peter Szrnka
 */
public record ServerConfig(
        int port,
        boolean sslEnabled,
        String serviceName,
        String serviceType,
        String serverDbFile,
        String env
) {
}
