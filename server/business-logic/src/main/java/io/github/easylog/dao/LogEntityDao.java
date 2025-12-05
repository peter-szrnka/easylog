package io.github.easylog.dao;

import io.github.easylog.model.LogEntry;
import io.github.easylog.model.PageResponse;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;

/**
 * @author Peter Szrnka
 */
public interface LogEntityDao {

    void save(SaveLogRequest request);

    PageResponse<LogEntry> findAll(SearchRequest searchRequest);
}
