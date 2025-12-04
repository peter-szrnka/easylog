package io.github.easylog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class DefaultWebsocketMessagingClientService implements WebsocketMessagingClientService {
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper;

    public void register(Object session) {
        if (session instanceof WebSocketSession ws) {
            sessions.add(ws);
        }
    }

    public void unregister(Object session) {
        if (session instanceof WebSocketSession ws) {
            sessions.remove(ws);
        }
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
