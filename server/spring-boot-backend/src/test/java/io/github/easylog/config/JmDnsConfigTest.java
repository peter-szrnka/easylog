package io.github.easylog.config;

import io.github.easylog.service.JmDnsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author Peter Szrnka
 */
@ExtendWith(MockitoExtension.class)
class JmDnsConfigTest {

    @Mock
    private JmDnsService jmDnsService;

    @InjectMocks
    private JmDnsConfig jmDnsConfig;


    @Test
    void init_shouldRegisterServiceSuccessfully() throws Exception {
        // when
        jmDnsConfig.init();

        // then
        verify(jmDnsService).init();
    }

    @Test
    void onShutdown_shouldUnregisterAndCloseJmDNS() {
        // when
        jmDnsConfig.onShutdown();

        // then
        verify(jmDnsService).onShutdown();
    }
}