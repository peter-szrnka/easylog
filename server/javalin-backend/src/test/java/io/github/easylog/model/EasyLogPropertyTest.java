package io.github.easylog.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Szrnka
 */
class EasyLogPropertyTest {

    @ParameterizedTest
    @EnumSource(EasyLogProperty.class)
    void testEnvironmentVariableValues(EasyLogProperty variable) {
        assertNotNull(variable, "Enum value should not be null");
        assertNotNull(variable.name(), "Enum name should not be null");

        String envName = getFieldValue(variable, "name");
        String defaultValue = getFieldValue(variable, "defaultValue");

        assertNotNull(envName, "Environment variable name should not be null");
        assertFalse(envName.isBlank(), "Environment variable name should not be blank");

        switch (variable) {
            case SERVER_PORT -> {
                assertEquals("SERVER_PORT", envName);
                assertEquals("8080", defaultValue);
            }
            case SSL_ENABLED -> {
                assertEquals("SSL_ENABLED", envName);
                assertEquals("true", defaultValue);
            }
            case SERVICE_NAME -> {
                assertEquals("SERVICE_NAME", envName);
                assertEquals("EasyLogService", defaultValue);
            }
            case SERVICE_TYPE -> {
                assertEquals("SERVICE_TYPE", envName);
                assertEquals("easyLog", defaultValue);
            }
            case SERVER_DB_FILE -> {
                assertEquals("SERVER_DB_FILE", envName);
                assertEquals("easylog.db", defaultValue);
            }
            case ENV -> {
                assertEquals("ENV", envName);
                assertEquals("", defaultValue);
            }
            case SSL_KEYSTORE -> {
                assertEquals("SSL_KEYSTORE", envName);
                assertNull(defaultValue);
            }
            case SSL_KEYSTORE_PASSWORD -> {
                assertEquals("SSL_KEYSTORE_PASSWORD", envName);
                assertNull(defaultValue);
            }
        }
    }

    private String getFieldValue(EasyLogProperty variable, String fieldName) {
        try {
            var field = EasyLogProperty.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(variable);
            return (String) value;
        } catch (Exception e) {
            fail("Unable to access field '" + fieldName + "': " + e.getMessage());
            return null;
        }
    }
}