package io.github.easylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Peter Szrnka
 */
class DefaultWebsocketMessagingClientServiceTest {

    private DefaultWebsocketMessagingClientService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        service = new DefaultWebsocketMessagingClientService(objectMapper);

        try {
            var field = DefaultWebsocketMessagingClientService.class.getDeclaredField("sessions");
            field.setAccessible(true);
            ((CopyOnWriteArrayList<?>) field.get(null)).clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void register_shouldAddWebSocketSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        service.register(session);

        assertThat(DefaultWebsocketMessagingClientService.getSessions().size()).isEqualTo(1);
    }

    @Test
    void unregister_shouldRemoveWebSocketSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        service.register(session);

        service.unregister(session);

        assertTrue(DefaultWebsocketMessagingClientService.getSessions().isEmpty());
    }

    @Test
    void convertAndSend_shouldSendMessageToAllSessions() throws Exception {
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        service.register(session1);
        service.register(session2);

        String payload = "hello";

        service.convertAndSend("/topic/test", payload);

        verify(session1, times(1)).sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        verify(session2, times(1)).sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }
}