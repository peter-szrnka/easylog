package io.github.easylog.entity;

import io.github.easylog.model.LogLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author Peter Szrnka
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "easylog_log")
public class LogEntity {

    @Id
    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private ZonedDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;

    @Column
    private String tag;

    @Column(columnDefinition = "TEXT")
    private String message;

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LogMetaDataEntity> metadata;
}
