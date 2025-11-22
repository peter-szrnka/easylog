package io.github.easylog.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String filter;
    private ZonedDateTime from;
    private ZonedDateTime to;
    private Pageable pageable;
}
