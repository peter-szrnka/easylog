package io.github.easylog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.json.JavalinJackson;
import io.javalin.websocket.WsContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultWebsocketMessagingClientService implements WebsocketMessagingClientService {
    private static final List<WsContext> sessions = new CopyOnWriteArrayList<>();

    public void register(WsContext ctx) {
        sessions.add(ctx);
    }

    public void unregister(WsContext ctx) {
        sessions.remove(ctx);
    }

    @Override
    public void convertAndSend(String destination, Object payload) {
        for (WsContext session : sessions) {
            try {
                session.send(JavalinJackson.defaultMapper().writeValueAsString(payload));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
