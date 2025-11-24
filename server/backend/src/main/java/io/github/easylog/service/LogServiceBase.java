package io.github.easylog.service;

import io.github.easylog.model.SaveLogRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Getter
@RequiredArgsConstructor
public abstract class LogServiceBase implements LogService {

    protected final SimpMessagingTemplate messagingTemplate;

    public void sendToWebSocket(SaveLogRequest request) {
        messagingTemplate.convertAndSend("/topic/logs", request);
    }
}
