package io.github.easylog.model;

import java.util.List;

/**
 * @author Peter Szrnka
 */
public class SaveLogRequest {
    private List<LogEntry> entries;

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }
}
