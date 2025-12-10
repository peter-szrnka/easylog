package io.github.easylog.model;

import java.util.List;

/**
 * @author Peter Szrnka
 */
public class SaveLogRequest {
    /**
     * Unique identifier of the request
     */
    private String requestId;
    private List<LogEntry> entries;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }
}
