package io.github.easylog.dao;

import io.github.easylog.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DefaultLogEntityDao implements LogEntityDao {

    private final Jdbi jdbi;

    public DefaultLogEntityDao(Jdbi jdbi) {
        this.jdbi = jdbi;
        initSchema();
    }

    private void initSchema() {
        jdbi.useHandle(handle -> {
            handle.execute("""
                CREATE TABLE IF NOT EXISTS easylog_log (
                    message_id VARCHAR PRIMARY KEY,
                    session_id VARCHAR,
                    level VARCHAR,
                    message VARCHAR,
                    tag VARCHAR,
                    timestamp TIMESTAMP
                )
            """);
            handle.execute("""
                CREATE TABLE IF NOT EXISTS easylog_log_metadata (
                    log_id VARCHAR,
                    key VARCHAR,
                    value VARCHAR,
                    PRIMARY KEY (log_id, key)
                )
            """);

            log.info("Database tables initialized");
        });
    }

    @Override
    public void save(SaveLogRequest request) {
        request.getEntries().forEach(entry -> {
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
                        .bind("timestamp", Timestamp.from(entry.getTimestamp().toInstant()))
                        .execute();

                if (entry.getMetadata() != null && !entry.getMetadata().isEmpty()) {
                    entry.getMetadata().forEach((key, value) -> {
                        handle.createUpdate("""
                                INSERT INTO easylog_log_metadata(log_id, key, value)
                                VALUES(:logId, :key, :value)
                                ON CONFLICT(log_id, key) DO UPDATE SET value = :value
                            """)
                                .bind("logId", entry.getMessageId())
                                .bind("key", key)
                                .bind("value", value)
                                .execute();
                    });
                }
            });
        });
    }

    @Override
    public PageResponse<LogEntry> findAll(SearchRequest searchRequest) {
        PageRequest pageRequest = searchRequest.getPageRequest();
        String filter = searchRequest.getFilter() == null ? "" : searchRequest.getFilter();
        List<LogEntry> entries = jdbi.withHandle(handle -> {
            String sql = """
                SELECT *
                    FROM easylog_log
                    WHERE :filter="" or (
                        lower(tag) like :filter OR
                        lower(message) like :filter OR
                        lower(message_id) like :filter OR
                        lower(session_id) like :filter
                        )
                    ORDER BY timestamp DESC LIMIT :limit OFFSET :offset
                """;
            return handle.createQuery(sql)
                    .bind("filter", filter.toLowerCase())
                    .bind("limit", pageRequest.getSize())
                    .bind("offset", pageRequest.getPage() * pageRequest.getSize())
                    .map((rs, ctx) -> {
                        LogEntry e = new LogEntry();
                        e.setMessageId(rs.getString("message_id"));
                        e.setSessionId(rs.getString("session_id"));
                        e.setLogLevel(LogLevel.valueOf(rs.getString("level")));
                        e.setMessage(rs.getString("message"));
                        e.setTag(rs.getString("tag"));
                        e.setTimestamp(rs.getTimestamp("timestamp").toInstant().atZone(ZoneId.systemDefault()));

                        // metadata
                        List<LogMetaData> metadata = handle.createQuery("""
                                SELECT key, value FROM easylog_log_metadata WHERE log_id = :logId
                            """).bind("logId", e.getMessageId())
                                .map((rs2, _) -> new LogMetaData(rs2.getString("key"), rs2.getString("value"))).list();

                        if (!metadata.isEmpty()) {
                            e.setMetadata(metadata.stream()
                                    .collect(Collectors.toMap(LogMetaData::key, LogMetaData::value)));
                        }

                        return e;
                    }).list();
        });

        // total count
        long total = jdbi.withHandle(handle -> handle.createQuery("SELECT COUNT(*) FROM easylog_log").mapTo(Long.class).one());
        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PageResponse.<LogEntry>builder()
                .content(entries)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
