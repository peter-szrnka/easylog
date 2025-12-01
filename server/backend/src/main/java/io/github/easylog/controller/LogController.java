package io.github.easylog.controller;

import io.github.easylog.model.*;
import io.github.easylog.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

/**
 * @author Peter Szrnka
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class LogController {

    private final LogService logService;

    @PostMapping
    public void save(@RequestBody SaveLogRequest request) {
        logService.save(request);
    }

    @GetMapping
    public PageResponse<LogEntry> list(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "dateRange", required = false) DateRange dateRange,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "sortBy") String sortBy,
            @RequestParam(name = "sortDirection") String sortDirection
    ) {
        return logService.list(SearchRequest.builder()
                .filter(filter)
                .dateRange(dateRange)
                .pageRequest(PageRequest.builder().page(page).size(size).sortBy(sortBy).sortDirection(sortDirection).build())
                .from(startDate)
                .to(endDate)
                .build()
        );
    }
}