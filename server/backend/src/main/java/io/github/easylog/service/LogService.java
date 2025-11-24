package io.github.easylog.service;

import io.github.easylog.model.LogEntry;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;
import org.springframework.data.domain.Page;

public interface LogService {

    void save(SaveLogRequest request);

    Page<LogEntry> list(SearchRequest searchRequest);
}
