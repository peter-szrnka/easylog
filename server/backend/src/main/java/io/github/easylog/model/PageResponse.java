package io.github.easylog.model;

import lombok.*;

import java.util.List;

/**
 * @author Peter Szrnka
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private int totalPages;
    private long totalElements;
    private List<T> content;
}
