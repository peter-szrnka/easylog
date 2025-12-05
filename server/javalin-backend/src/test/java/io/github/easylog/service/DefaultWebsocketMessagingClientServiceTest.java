package io.github.easylog.service;

import io.javalin.websocket.WsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Peter Szrnka
 */
class DefaultWebsocketMessagingClientServiceTest {

    private DefaultWebsocketMessagingClientService service;

    @BeforeEach
    void setup() {
        service = new DefaultWebsocketMessagingClientService();

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
        // given
        WsContext session = mock(WsContext.class);

        // when
        service.register(session);

        // then
        assertThat(DefaultWebsocketMessagingClientService.getSessions().size()).isEqualTo(1);
    }

    @Test
    void unregister_shouldRemoveWebSocketSession() {
        // given
        WsContext session = mock(WsContext.class);
        service.register(session);

        // when
        service.unregister(session);

        // then
        assertTrue(DefaultWebsocketMessagingClientService.getSessions().isEmpty());
    }

    @Test
    void convertAndSend_shouldSendMessageToAllSessions() {
        // given
        WsContext session1 = mock(WsContext.class);
        WsContext session2 = mock(WsContext.class);

        service.register(session1);
        service.register(session2);

        String payload = "{\"key\":\"username\",\"value\":\"john.doe\"}";

        // when
        service.convertAndSend("/topic/test", new Data("username", "john.doe"));

        // then
        verify(session1).send(payload);
        verify(session2).send(payload);
    }

    @Test
    void convertAndSend_shouldWrapExceptionInRuntimeException() {
        // given
        WsContext session = mock(WsContext.class);
        doThrow(new RuntimeException("fail")).when(session).send(anyString());

        // when
        service.register(session);

        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.convertAndSend("/topic/test", "payload"));
        assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: fail");
    }
}