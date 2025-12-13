package io.github.easylog.model;

/**
 * @author Peter Szrnka
 *
 * @param port Server port
 * @param sslEnabled SSL enabled or disabled (by default it's enabled)
 * @param serviceName NSD Service name
 * @param serviceType NSD service type
 * @param serverDbFile Server db file, e.g.: easylog-local.db
 * @param env Environment
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
