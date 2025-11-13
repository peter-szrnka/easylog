package hu.peterszrnka.easylog.service;

import hu.peterszrnka.easylog.model.SaveLogRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!file-db")
public class SimpleLogService extends LogServiceBase {

    public SimpleLogService(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Override
    public void save(SaveLogRequest request) {
        log.info("Received log: {}", request);
        sendToWebSocket(request);
    }
}
