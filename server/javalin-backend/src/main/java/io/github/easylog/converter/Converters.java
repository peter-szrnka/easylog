package io.github.easylog.converter;

import io.github.easylog.model.DateRangeType;
import io.javalin.http.Context;

import java.time.ZonedDateTime;

public class Converters {

    public static DateRangeType converDateRangeType(Context ctx, String key) {
        String value = ctx.queryParam("key");

        DateRangeType dateRangeType = null;
        if (value != null && !value.isEmpty()) {
            dateRangeType = DateRangeType.valueOf(value);
        }

        return dateRangeType != null ? dateRangeType : DateRangeType.LAST_15_MINUTES;
    }

    public static ZonedDateTime convertZonedDateTime(Context ctx, String key) {
        String value = ctx.queryParam("key");

        ZonedDateTime date = null;
        if (value != null && !value.isEmpty()) {
            date = ZonedDateTime.parse(value);
        }

        return date;
    }
}
