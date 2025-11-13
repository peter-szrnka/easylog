package hu.peterszrnka.easylog.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author Peter Szrnka
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SaveLogRequest {
    private String correlationId;
    private String sessionId;
    private LogLevel logLevel;
    private String tag;
    private String message;
    private Map<String, String> metadata;
}
