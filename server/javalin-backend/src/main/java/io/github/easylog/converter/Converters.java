package io.github.easylog.converter;

import io.github.easylog.model.DateRangeType;
import io.javalin.http.Context;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Peter Szrnka
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Converters {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    public static DateRangeType convertDateRangeType(Context ctx, String key) {
        String value = ctx.queryParam(key);

        DateRangeType dateRangeType = null;
        if (value != null && !value.isEmpty()) {
            dateRangeType = DateRangeType.valueOf(value);
        }

        return dateRangeType != null ? dateRangeType : DateRangeType.LAST_15_MINUTES;
    }

    public static ZonedDateTime convertZonedDateTime(Context ctx, String key) {
        String value = ctx.queryParam(key);

        ZonedDateTime date = null;
        if (value != null && !value.isEmpty()) {
            date = ZonedDateTime.parse(value.replace(" ", "+"), FORMATTER);
        }

        return date;
    }
}
