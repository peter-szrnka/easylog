package io.github.easylog.model;

import lombok.*;

/**
 * @author Peter Szrnka
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    private int page;
    private int size;
    @Builder.Default
    private String sortBy = "timestamp";
    @Builder.Default
    private String sortDirection = "desc";
}
