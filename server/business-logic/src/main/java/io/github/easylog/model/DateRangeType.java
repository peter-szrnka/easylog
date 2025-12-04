package io.github.easylog.model;

import java.time.ZonedDateTime;

/**
 * @author Peter Szrnka
 */
public enum DateRangeType {
    CUSTOM,
    LIVE,
    LAST_5_MINUTES,
    LAST_15_MINUTES,
    LAST_30_MINUTES,
    LAST_1_HOUR,
    LAST_4_HOURS,
    LAST_1_DAY,
    LAST_7_DAYS,
    LAST_1_MONTH;

    public static ZonedDateTime from(DateRangeType dateRangeType, ZonedDateTime from) {
        if (CUSTOM == dateRangeType) {
            return from;
        }

        ZonedDateTime now = ZonedDateTime.now();
        return switch (dateRangeType) {
            case LAST_5_MINUTES -> now.minusMinutes(5);
            case LAST_30_MINUTES -> now.minusMinutes(30);
            case LAST_1_HOUR -> now.minusHours(1);
            case LAST_4_HOURS -> now.minusHours(4);
            case LAST_1_DAY -> now.minusDays(1);
            case LAST_7_DAYS -> now.minusDays(7);
            case LAST_1_MONTH -> now.minusMonths(1);
            default ->  now.minusMinutes(15);
        };
    }
}
