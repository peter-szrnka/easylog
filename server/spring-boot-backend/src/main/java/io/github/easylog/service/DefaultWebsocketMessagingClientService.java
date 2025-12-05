package io.github.easylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Peter Szrnka
 */
@Service
@RequiredArgsConstructor
public class DefaultWebsocketMessagingClientService implements WebsocketMessagingClientService {
    @Getter(AccessLevel.PROTECTED)
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper;

    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }


    @Override
    public void convertAndSend(String destination, Object payload) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
