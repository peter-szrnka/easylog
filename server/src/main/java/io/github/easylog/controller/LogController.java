package io.github.easylog.controller;

import io.github.easylog.model.LogEntry;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;
import io.github.easylog.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

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
    public Page<LogEntry> list(
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "sortBy") String sortBy,
            @RequestParam(name = "sortDirection") String sortDirection
    ) {
        Sort.Order order = ("desc".equalsIgnoreCase(sortDirection)) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy);
        return logService.list(SearchRequest.builder()
                .filter(filter)
                .pageable(PageRequest.of(page, size, Sort.by(order)))
                .from(startDate)
                .to(endDate)
                .build()
        );
    }
}