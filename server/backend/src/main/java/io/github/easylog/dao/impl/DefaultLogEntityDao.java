package io.github.easylog.dao.impl;

import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.entity.LogEntity;
import io.github.easylog.data.LogEntityRepository;
import io.github.easylog.data.LogSpecification;
import io.github.easylog.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

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
                .findAll(LogSpecification.search(searchRequest.getFilter(), from(dateRangeType, searchRequest.getFrom()), searchRequest.getTo()), mapToPageable(searchRequest.getPageRequest()))
                .map(DefaultLogEntityDao::mapToDto);

        return PageResponse.<LogEntry>builder()
                .content(results.getContent())
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .build();
    }

    private static ZonedDateTime from(DateRangeType dateRangeType, ZonedDateTime from) {
        if (DateRangeType.CUSTOM == dateRangeType) {
            return from;
        }

        ZonedDateTime now = ZonedDateTime.now();
        return switch (dateRangeType) {
            case LAST_5_MINUTES -> now.minusMinutes(5);
            case LAST_15_MINUTES -> now.minusMinutes(16);
            case LAST_30_MINUTES -> now.minusMinutes(30);
            case LAST_1_HOUR -> now.minusHours(1);
            case LAST_4_HOURS -> now.minusHours(4);
            case LAST_1_DAY -> now.minusDays(1);
            case LAST_7_DAYS -> now.minusDays(7);
            case LAST_1_MONTH -> now.minusMonths(1);
            default ->  now.minusMinutes(15);
        };
    }

    private static Pageable mapToPageable(io.github.easylog.model.PageRequest pageRequest) {
        String sortBy = pageRequest.getSortBy();
        Sort.Order order = ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy);
        return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(order));
    }

    private static LogEntity mapToEntity(LogEntry item) {
        return LogEntity.builder()
                .level(item.getLogLevel())
                .message(item.getMessage())
                .timestamp(item.getTimestamp())
                .tag(item.getTag())
                .sessionId(item.getSessionId())
                .messageId(item.getMessageId())
                .build();
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
