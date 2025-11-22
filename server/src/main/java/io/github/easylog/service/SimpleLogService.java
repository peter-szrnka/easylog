package io.github.easylog.service;

import io.github.easylog.model.LogEntry;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Profile("!file-db")
public class SimpleLogService extends LogServiceBase {

    private final List<LogEntry> logs = new ArrayList<>();

    public SimpleLogService(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Override
    public void save(SaveLogRequest request) {
        log.info("Received logs: {}", request);
        logs.addAll(request.getEntries());
        sendToWebSocket(request);
    }

    @Override
    public Page<LogEntry> list(SearchRequest searchRequest) {
        return new PageImpl<>(logs, searchRequest.getPageable(), logs.size());
    }
}
