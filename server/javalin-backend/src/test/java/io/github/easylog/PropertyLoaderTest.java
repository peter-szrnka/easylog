package io.github.easylog;

import io.github.easylog.model.ServerConfig;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Szrnka
 */
class PropertyLoaderTest {

    @Test
    void loadProperties_whenFileIsMissing_thenThrowException() {
        assertThrows(FileNotFoundException.class, () -> EasyLogApplication.loadProperties("unknown"));
    }

    @Test
    void loadProperties() throws IOException {
        // when
        ServerConfig serverConfig = EasyLogApplication.loadProperties("src/test/resources/application.properties");

        // then
        assertEquals(8081, serverConfig.port());
        assertTrue(serverConfig.sslEnabled());
        assertEquals("EasyLogService", serverConfig.serviceName());
        assertEquals("easyLog", serverConfig.serviceType());
        assertEquals("easylog-test.db", serverConfig.serverDbFile());
    }
}
