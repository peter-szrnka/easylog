package io.github.easylog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Peter Szrnka
 */
@Getter
@RequiredArgsConstructor
public enum EasyLogProperty {
    SERVER_PORT("SERVER_PORT", "8080"),
    SSL_ENABLED("SSL_ENABLED", "true"),
    SERVICE_NAME("SERVICE_NAME", "EasyLogService"),
    SERVICE_TYPE("SERVICE_TYPE", "easyLog"),
    SERVER_DB_FILE("SERVER_DB_FILE", "easylog.db"),
    ENV("ENV", ""),
    SSL_KEYSTORE("SSL_KEYSTORE", null),
    SSL_KEYSTORE_PASSWORD("SSL_KEYSTORE_PASSWORD", null);

    private final String name;
    private final String defaultValue;
}
