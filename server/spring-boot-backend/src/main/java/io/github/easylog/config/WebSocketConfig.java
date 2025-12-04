package io.github.easylog.config;


import io.github.easylog.service.DefaultWebsocketMessagingClientService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author Peter Szrnka
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DefaultWebsocketMessagingClientService websocketMessagingClientService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
                        websocketMessagingClientService.register(session);
                    }

                    @Override
                    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull org.springframework.web.socket.CloseStatus status) {
                        websocketMessagingClientService.unregister(session);
                    }
                }, "/topic/logs")
                .setAllowedOriginPatterns("*");
    }
}