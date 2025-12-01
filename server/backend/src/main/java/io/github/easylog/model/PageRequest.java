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
    private String sortBy = "t";
    private String sortDirection = "desc";
}
