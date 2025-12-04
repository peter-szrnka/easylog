package io.github.easylog.controller;

import io.github.easylog.model.DateRangeType;
import io.github.easylog.model.PageRequest;
import io.github.easylog.model.SaveLogRequest;
import io.github.easylog.model.SearchRequest;
import io.github.easylog.service.LogService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

import static io.github.easylog.converter.Converters.converDateRangeType;
import static io.github.easylog.converter.Converters.convertZonedDateTime;

@Slf4j
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    public void registerRoutes(Javalin app) {
        app.post("/api/log", this::saveLog);
        app.get("/api/log", this::listLogs);
        log.info("Routes registered for {}", getClass().getSimpleName());
    }

    private void saveLog(Context ctx) {
        SaveLogRequest request = ctx.bodyAsClass(SaveLogRequest.class);
        logService.save(request);
        ctx.status(201);
    }

    private void listLogs(Context ctx) {
        ZonedDateTime from = convertZonedDateTime(ctx, "startDate");
        ZonedDateTime to =  convertZonedDateTime(ctx, "endDate");
        DateRangeType dateRangeType = converDateRangeType(ctx, "dateRangeType");

        SearchRequest req = SearchRequest.builder()
                .filter(ctx.queryParam("filter"))
                .pageRequest(PageRequest.builder()
                        .page(ctx.queryParamAsClass("page", Integer.class).getOrDefault(0))
                        .size(ctx.queryParamAsClass("size", Integer.class).getOrDefault(50))
                        .sortBy(ctx.queryParamAsClass("sortBy", String.class).getOrDefault("timestamp"))
                        .sortDirection(ctx.queryParamAsClass("sortDirection", String.class).getOrDefault("desc"))
                        .build())
                .from(from)
                .to(to)
                .dateRangeType(dateRangeType)
                .build();

        var page = logService.list(req);
        ctx.json(page);
    }
}