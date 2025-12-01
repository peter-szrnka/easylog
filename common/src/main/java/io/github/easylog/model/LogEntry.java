package io.github.easylog.model;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * @author Peter Szrnka
 */
public class LogEntry {

    private String messageId;
    private String sessionId;
    private LogLevel logLevel;
    private ZonedDateTime timestamp;
    private String tag;
    private String message;
    private Map<String, String> metadata;

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                ", messageId='" + messageId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", logLevel=" + logLevel +
                ", timestamp=" + timestamp +
                ", tag='" + tag + '\'' +
                ", message='" + message + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
