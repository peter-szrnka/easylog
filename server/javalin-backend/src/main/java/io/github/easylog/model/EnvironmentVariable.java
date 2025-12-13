package io.github.easylog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Peter Szrnka
 */
@Getter
@RequiredArgsConstructor
public enum EnvironmentVariable {
    SERVER_PORT("EASYLOG_SERVER_PORT", "8080"),
    SSL_ENABLED("EASYLOG_SSL_ENABLED", "true"),
    SERVICE_NAME("EASYLOG_SERVICE_NAME", "EasyLogService"),
    SERVICE_TYPE("EASYLOG_SERVICE_TYPE", "easyLog"),
    SERVER_DB_FILE("EASYLOG_SERVER_DB_FILE", "easylog.db"),
    ENV("EASYLOG_ENV", ""),
    SSL_KEYSTORE("EASYLOG_SSL_KEYSTORE", null),
    SSL_KEYSTORE_PASSWORD("EASYLOG_SSL_KEYSTORE_PASSWORD", null);

    private final String name;
    private final String defaultValue;
}
