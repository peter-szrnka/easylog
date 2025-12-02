package io.github.easylog.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author Peter Szrnka
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "easylog_log_metadata")
public class LogMetaDataEntity {

    @Id
    @Column(length = 26, unique = true, nullable = false)
    private String id;

    @JoinColumn(name = "log_id", nullable = false)
    @ManyToOne
    private LogEntity log;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UlidCreator.getUlid().toString();
        }
    }
}
