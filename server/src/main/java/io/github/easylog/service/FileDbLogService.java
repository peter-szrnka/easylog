package io.github.easylog.service;

import io.github.easylog.data.LogEntity;
import io.github.easylog.data.LogEntityRepository;
import io.github.easylog.data.LogSpecification;
import io.github.easylog.model.LogEntry;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("file-db")
public class FileDbLogService extends LogServiceBase {

    private final LogEntityRepository repository;

    public FileDbLogService(
            SimpMessagingTemplate messagingTemplate,
            LogEntityRepository repository
    ) {
        super(messagingTemplate);
        this.repository = repository;
    }

    @Override
    public void save(SaveLogRequest request) {
        request.getEntries().forEach(item -> {
            log.info("Received log: {}", item);
            repository.save(mapToEntity(item));
        });

        sendToWebSocket(request);
    }

    @Override
    public Page<LogEntry> list(SearchRequest searchRequest) {
        return repository
                .findAll(LogSpecification.search(searchRequest.getFilter(), searchRequest.getFrom(), searchRequest.getTo()), searchRequest.getPageable()).map(this::mapToDto);
    }

    private LogEntry mapToDto(LogEntity entity) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLogLevel(entity.getLevel());
        logEntry.setMessage(entity.getMessage());
        logEntry.setTimestamp(entity.getTimestamp());
        logEntry.setTag(entity.getTag());
        logEntry.setSessionId(entity.getSessionId());
        logEntry.setCorrelationId(entity.getCorrelationId());

        return logEntry;
    }

    private LogEntity mapToEntity(LogEntry item) {
        return LogEntity.builder()
                .level(item.getLogLevel())
                .message(item.getMessage())
                .timestamp(item.getTimestamp())
                .tag(item.getTag())
                .sessionId(item.getSessionId())
                .correlationId(item.getCorrelationId())
                .build();
    }
}
