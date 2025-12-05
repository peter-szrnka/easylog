package io.github.easylog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JmDnsServiceTest {

    private JmDNS jmDNS;
    private JmDnsService service;

    @BeforeEach
    void setup() {
        jmDNS = Mockito.mock(JmDNS.class);
        service = new JmDnsService("easylog-service", 8080, "_easylog._tcp.local.", true) {
            @Override
            protected JmDNS createJmDNS(InetAddress address) {
                return jmDNS;
            }
        };
    }

    @Test
    void createServiceInfo_shouldBuildExpectedServiceInfo() {
        // when
        ServiceInfo info = invokePrivateCreateServiceInfo();

        // then
        assertThat(info.getName()).isEqualTo("easylog-service");
        assertThat(info.getType()).isEqualTo("_easylog._tcp.local.");
        assertThat(info.getPort()).isEqualTo(8080);
        assertThat(info.getPropertyString("path")).isEqualTo("/api/log");
        assertThat(info.getPropertyString("ssl")).isEqualTo("true");
    }

    @Test
    void init_shouldRegisterServiceSuccessfully() throws Exception {
        InetAddress fakeAddress = InetAddress.getByName("127.0.0.1");

        try (MockedStatic<InetAddress> inetStatic = mockStatic(InetAddress.class)) {
            inetStatic.when(InetAddress::getLocalHost).thenReturn(fakeAddress);

            service.init();

            verify(jmDNS).registerService(any(ServiceInfo.class));
        }
    }

    @Test
    void onShutdown_shouldUnregisterAndCloseJmDNS() throws Exception {
        // given
        service.init();

        // when
        service.onShutdown();

        // then
        verify(jmDNS).unregisterAllServices();
        verify(jmDNS).close();
    }

    @Test
    void onShutdown_shouldHandleExceptionGracefully() throws Exception {
        // given
        service.init();
        doThrow(new IOException("close error")).when(jmDNS).close();

        // when
        service.onShutdown();

        // then
        verify(jmDNS).unregisterAllServices();
        verify(jmDNS).close();
    }

    @Test
    void onShutdown_whenJmDnsIsNull() throws Exception {
        // when
        service.onShutdown();

        // then
        verify(jmDNS, never()).unregisterAllServices();
        verify(jmDNS, never()).close();
    }

    private ServiceInfo invokePrivateCreateServiceInfo() {
        try {
            var method = JmDnsService.class.getDeclaredMethod("createServiceInfo");
            method.setAccessible(true);
            return (ServiceInfo) method.invoke(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}