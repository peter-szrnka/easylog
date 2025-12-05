package io.github.easylog.dao;

import io.github.easylog.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Peter Szrnka
 */
@Slf4j
public class DefaultLogEntityDao implements LogEntityDao {

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
                FROM easylog_log
                WHERE :filter="" or (
                    lower(tag) like :filter OR
                    lower(message) like :filter OR
                    lower(message_id) like :filter OR
                    lower(session_id) like :filter
                    )
            """;
    private static final String FIND_QUERY = """
            SELECT *
                FROM easylog_log
                WHERE :filter="" or (
                    LOWER(tag) LIKE :filter OR
                    LOWER(message) LIKE :filter OR
                    LOWER(message_id) LIKE :filter OR
                    LOWER(session_id) LIKE :filter
                    )
                    AND (
                        :from is null or :from >= timestamp OR
                        :to is null or :to <= timestamp
                    )
                ORDER BY timestamp DESC LIMIT :limit OFFSET :offset
            """;
    private static final String CREATE_LOG_TABLE = """
                CREATE TABLE IF NOT EXISTS easylog_log (
                    message_id VARCHAR PRIMARY KEY,
                    session_id VARCHAR,
                    level VARCHAR,
                    message VARCHAR,
                    tag VARCHAR,
                    timestamp TIMESTAMP
                )
            """;
    private static final String CREATE_LOG_METADATA_TABLE = """
                CREATE TABLE IF NOT EXISTS easylog_log_metadata (
                    log_id VARCHAR,
                    key VARCHAR,
                    value VARCHAR,
                    PRIMARY KEY (log_id, key)
                )
            """;
    private final Jdbi jdbi;

    public DefaultLogEntityDao(Jdbi jdbi) {
        this.jdbi = jdbi;
        initSchema();
    }

    private void initSchema() {
        jdbi.useHandle(handle -> {
            handle.execute(CREATE_LOG_TABLE);
            handle.execute(CREATE_LOG_METADATA_TABLE);

            log.info("Database tables initialized");
        });
    }

    @Override
    public void save(SaveLogRequest request) {
        request.getEntries().forEach(entry -> {
            ZonedDateTime timestamp = Optional.ofNullable(entry.getTimestamp()).orElse(ZonedDateTime.now());
            jdbi.useHandle(handle -> {
                handle.createUpdate("""
                                    INSERT INTO easylog_log(message_id, session_id, level, message, tag, timestamp)
                                    VALUES(:messageId, :sessionId, :level, :message, :tag, :timestamp)
                                    ON CONFLICT(message_id) DO NOTHING
                                """)
                        .bind("messageId", entry.getMessageId())
                        .bind("sessionId", entry.getSessionId())
                        .bind("level", entry.getLogLevel())
                        .bind("message", entry.getMessage())
                        .bind("tag", entry.getTag())
                        .bind("timestamp", Timestamp.from(timestamp.toInstant()))
                        .execute();

                if (entry.getMetadata() != null && !entry.getMetadata().isEmpty()) {
                    entry.getMetadata().forEach((key, value) -> handle.createUpdate("""
                                        INSERT INTO easylog_log_metadata(log_id, key, value)
                                        VALUES(:logId, :key, :value)
                                        ON CONFLICT(log_id, key) DO UPDATE SET value = :value
                                    """)
                            .bind("logId", entry.getMessageId())
                            .bind("key", key)
                            .bind("value", value)
                            .execute());
                }
            });
        });
    }

    @Override
    public PageResponse<LogEntry> findAll(SearchRequest searchRequest) {
        PageRequest pageRequest = searchRequest.getPageRequest();
        String filter = searchRequest.getFilter() == null ? "" : "%" + searchRequest.getFilter() + "%";
        List<LogEntry> entries = jdbi.withHandle(handle -> handle.createQuery(FIND_QUERY)
                .bind("filter", filter.toLowerCase())
                .bind("limit", pageRequest.getSize())
                .bind("offset", pageRequest.getPage() * pageRequest.getSize())
                .bind("from", convertToNullSafe(searchRequest.getFrom()))
                .bind("to", convertToNullSafe(searchRequest.getTo()))
                .map((rs, _) -> mapToLogEntry(handle, rs)).list());

        long total = jdbi.withHandle(handle -> countResults(handle, filter).mapTo(Long.class).one());
        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PageResponse.<LogEntry>builder()
                .content(entries)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private Timestamp convertToNullSafe(ZonedDateTime input) {
        return (input == null) ? null : Timestamp.from(input.toInstant());
    }

    private LogEntry mapToLogEntry(Handle handle, ResultSet rs) throws SQLException {
        LogEntry e = new LogEntry();
        e.setMessageId(rs.getString("message_id"));
        e.setSessionId(rs.getString("session_id"));
        e.setLogLevel(LogLevel.valueOf(rs.getString("level")));
        e.setMessage(rs.getString("message"));
        e.setTag(rs.getString("tag"));
        e.setTimestamp(rs.getTimestamp("timestamp").toInstant().atZone(ZoneId.systemDefault()));

        // metadata
        try (Query query = handle.createQuery("""
                                SELECT key, value FROM easylog_log_metadata WHERE log_id = :logId
                            """)) {
            List<LogMetaData> metadata = query.bind("logId", e.getMessageId())
                    .map((rs2, _) -> new LogMetaData(rs2.getString("key"), rs2.getString("value"))).list();

            if (!metadata.isEmpty()) {
                e.setMetadata(metadata.stream()
                        .collect(Collectors.toMap(LogMetaData::key, LogMetaData::value)));
            }
        }

        return e;
    }

    private static Query countResults(Handle handle, String filter) {
        try (Query query = handle.createQuery(COUNT_SQL)) {
            return query.bind("filter", filter.toLowerCase());
        }
    }
}
