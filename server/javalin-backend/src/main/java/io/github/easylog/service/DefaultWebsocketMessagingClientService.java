package io.github.easylog.service;

import io.javalin.json.JavalinJackson;
import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Peter Szrnka
 */
public class DefaultWebsocketMessagingClientService implements WebsocketMessagingClientService {
    @Getter(AccessLevel.PROTECTED)
    private static final List<WsContext> sessions = new CopyOnWriteArrayList<>();

    public void register(WsContext ctx) {
        sessions.add(ctx);
    }

    public void unregister(WsContext ctx) {
        sessions.remove(ctx);
    }

    @Override
    public void convertAndSend(String destination, Object payload) throws Exception {
        for (WsContext session : sessions) {
            session.send(JavalinJackson.defaultMapper().writeValueAsString(payload));
        }
    }
}
