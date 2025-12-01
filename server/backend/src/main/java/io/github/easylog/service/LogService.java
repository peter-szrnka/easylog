package io.github.easylog.service;

import io.github.easylog.model.LogEntry;
import io.github.easylog.model.PageResponse;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;

/**
 * @author Peter Szrnka
 */
public interface LogService {

    void save(SaveLogRequest request);

    PageResponse<LogEntry> list(SearchRequest searchRequest);
}
