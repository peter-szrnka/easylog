package io.github.easylog.dao;

import io.github.easylog.data.LogEntityRepository;
import io.github.easylog.data.LogSpecification;
import io.github.easylog.entity.LogEntity;
import io.github.easylog.entity.LogMetaDataEntity;
import io.github.easylog.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Data JPA based implementation
 * @author Peter Szrnka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLogEntityDao implements LogEntityDao {

    private final LogEntityRepository repository;

    @Override
    public void save(SaveLogRequest request) {
        request.getEntries().forEach(item -> {
            log.info("Received log: {}", item);
            repository.save(mapToEntity(item));
        });
    }

    @Override
    public PageResponse<LogEntry> findAll(SearchRequest searchRequest) {
        DateRangeType dateRangeType = searchRequest.getDateRangeType();

        Page<LogEntry> results = repository
                .findAll(LogSpecification.search(searchRequest.getFilter(), DateRangeType.from(dateRangeType, searchRequest.getFrom()), searchRequest.getTo()), mapToPageable(searchRequest.getPageRequest()))
                .map(DefaultLogEntityDao::mapToDto);

        return PageResponse.<LogEntry>builder()
                .content(results.getContent())
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .build();
    }

    private static Pageable mapToPageable(io.github.easylog.model.PageRequest pageRequest) {
        String sortBy = pageRequest.getSortBy();
        Sort.Order order = ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy);
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(order));
    }

    private static LogEntity mapToEntity(LogEntry item) {
        LogEntity entity = LogEntity.builder()
                .level(item.getLogLevel())
                .message(item.getMessage())
                .timestamp(item.getTimestamp())
                .tag(item.getTag())
                .sessionId(item.getSessionId())
                .messageId(item.getMessageId())
                .build();

        entity.setMetadata(mapMetadata(entity, item.getMetadata()));
        return entity;
    }

    private static Set<LogMetaDataEntity> mapMetadata(LogEntity entity, Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        return metadata.entrySet().stream().map(item -> LogMetaDataEntity.builder().log(entity).key(item.getKey()).value(item.getValue()).build()).collect(Collectors.toSet());
    }

    private static LogEntry mapToDto(LogEntity entity) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLogLevel(entity.getLevel());
        logEntry.setMessage(entity.getMessage());
        logEntry.setTimestamp(entity.getTimestamp());
        logEntry.setTag(entity.getTag());
        logEntry.setSessionId(entity.getSessionId());
        logEntry.setMessageId(entity.getMessageId());

        return logEntry;
    }
}
