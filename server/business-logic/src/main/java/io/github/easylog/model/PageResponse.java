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
    @Builder.Default
    private int totalPages = 0;
    @Builder.Default
    private long totalElements = 0;
    @Builder.Default
    private List<T> content = List.of();
}
