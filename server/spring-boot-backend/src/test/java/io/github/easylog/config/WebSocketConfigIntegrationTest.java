package io.github.easylog.config;

import io.github.easylog.service.DefaultWebsocketMessagingClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class WebSocketConfigIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private DefaultWebsocketMessagingClientService websocketMessagingClientService;

    private WebSocketClient client;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new StringMessageConverter());
    }

    @Test
    void whenClientConnects_thenRegisterAndUnregisterAreCalled() throws Exception {
        // WebSocket URL
        String wsUrl = String.format("ws://localhost:%d/topic/logs", port);

        // connect
        WebSocketSession session = client
                .execute(new TextWebSocketHandler() {}, wsUrl)
                .get(3, TimeUnit.SECONDS);

        // várunk egy kicsit, hogy a register hívás megtörténjen
        Thread.sleep(200);

        verify(websocketMessagingClientService, timeout(1000)).register(any(WebSocketSession.class));

        // kapcsolat lezárása
        session.close();
        Thread.sleep(200);

        verify(websocketMessagingClientService, timeout(1000)).unregister(any(WebSocketSession.class));
    }
}