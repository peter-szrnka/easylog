package io.github.easylog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * @author Peter Szrnka
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String filter;
    private DateRangeType dateRangeType;
    private ZonedDateTime from;
    private ZonedDateTime to;
    private PageRequest pageRequest;
}
