package io.github.easylog.service;

import io.github.easylog.dao.LogEntityDao;
import io.github.easylog.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Szrnka
 */
@Slf4j
@RequiredArgsConstructor
public class LogService {

    private final WebsocketMessagingClientService websocketMessagingClientService;
    private final LogEntityDao dao;

    public void save(SaveLogRequest request) {
        dao.save(request);
        sendToWebSocket(request);
    }

    public PageResponse<LogEntry> list(SearchRequest searchRequest) {
        DateRangeType dateRangeType = searchRequest.getDateRangeType();

        if (DateRangeType.LIVE == dateRangeType) {
            return PageResponse.<LogEntry>builder().totalElements(0L).totalPages(0).build();
        }

        return dao.findAll(searchRequest);
    }

    private void sendToWebSocket(SaveLogRequest request) {
        try {
            websocketMessagingClientService.convertAndSend("/topic/logs", request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
