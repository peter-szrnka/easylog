package io.github.easylog.config;

import io.github.easylog.service.JmDnsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JmDnsConfigTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private JmDnsConfig jmDnsConfig;

    @BeforeEach
    void setup() {
        JmDnsService service = mock(JmDnsService.class);
        jmDnsConfig = new JmDnsConfig(service);
        ReflectionTestUtils.setField(jmDnsConfig, "port", 8080);
        ReflectionTestUtils.setField(jmDnsConfig, "serviceName", "easylog-service");
        ReflectionTestUtils.setField(jmDnsConfig, "serviceType", "_easylog._tcp.local.");
        ReflectionTestUtils.setField(jmDnsConfig, "sslEnabled", true);
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

        try (MockedStatic<JmDNS> jmDNSStatic = mockStatic(JmDNS.class)) {
            JmDNS mockJmDNS = mock(JmDNS.class);
            jmDNSStatic.when(() -> JmDNS.create(fakeAddress)).thenReturn(mockJmDNS);

            jmDnsConfig = spy(jmDnsConfig);

            try (MockedStatic<InetAddress> inetStatic = mockStatic(InetAddress.class)) {
                inetStatic.when(InetAddress::getLocalHost).thenReturn(fakeAddress);

                jmDnsConfig.init();

                verify(mockJmDNS).registerService(any(ServiceInfo.class));
                assertThat(jmDnsConfig).hasFieldOrPropertyWithValue("jmdns", mockJmDNS);
            }
        }
    }

    @Test
    void onShutdown_shouldUnregisterAndCloseJmDNS() throws Exception {
        // given
        JmDNS mockJmDNS = mock(JmDNS.class);
        ReflectionTestUtils.setField(jmDnsConfig, "jmdns", mockJmDNS);

        // when
        jmDnsConfig.onShutdown();

        // then
        verify(mockJmDNS).unregisterAllServices();
        verify(mockJmDNS).close();
    }

    @Test
    void onShutdown_shouldHandleExceptionGracefully() throws Exception {
        // given
        JmDNS mockJmDNS = mock(JmDNS.class);
        doThrow(new IOException("close error")).when(mockJmDNS).close();
        ReflectionTestUtils.setField(jmDnsConfig, "jmdns", mockJmDNS);

        // when
        jmDnsConfig.onShutdown();

        // then
        verify(mockJmDNS).unregisterAllServices();
        verify(mockJmDNS).close();
    }

    @Test
    void onShutdown_whenJmDnsIsNull() throws Exception {
        // given
        JmDNS mockJmDNS = mock(JmDNS.class);
        ReflectionTestUtils.setField(jmDnsConfig, "jmdns", null);

        // when
        jmDnsConfig.onShutdown();

        // then
        verify(mockJmDNS, never()).unregisterAllServices();
        verify(mockJmDNS, never()).close();
    }

    private ServiceInfo invokePrivateCreateServiceInfo() {
        try {
            var method = JmDnsConfig.class.getDeclaredMethod("createServiceInfo");
            method.setAccessible(true);
            return (ServiceInfo) method.invoke(jmDnsConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}