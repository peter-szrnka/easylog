package io.github.easylog.converter;

import io.github.easylog.model.DateRangeType;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class ConvertersTest {

    private final Context ctx = Mockito.mock(Context.class);

    @Test
    void convertDateRangeType_whenDateRangeNotAvailable_thenReturnDefault() {
        // when
        DateRangeType result = Converters.convertDateRangeType(ctx, "dateRangeType");

        // then
        assertEquals(DateRangeType.LAST_15_MINUTES, result);
    }

    @Test
    void convertDateRangeType_whenDateRangeIsEmpty_thenReturn() {
        // given
        when(ctx.queryParam("dateRangeType")).thenReturn("");

        // when
        DateRangeType result = Converters.convertDateRangeType(ctx, "dateRangeType");

        // then
        assertEquals(DateRangeType.LAST_15_MINUTES, result);
    }

    @Test
    void convertDateRangeType_whenDateRangeAvailable_thenReturn() {
        // given
        when(ctx.queryParam("dateRangeType")).thenReturn("CUSTOM");

        // when
        DateRangeType result = Converters.convertDateRangeType(ctx, "dateRangeType");

        // then
        assertEquals(DateRangeType.CUSTOM, result);
    }

    @Test
    void convertZonedDateTime_whenDateRangeNotAvailable_thenReturnDefault() {
        // when
        ZonedDateTime result = Converters.convertZonedDateTime(ctx, "from");

        // then
        assertNull(result);
    }

    @Test
    void convertZonedDateTime_whenDateRangeIsEmpty_thenReturn() {
        // given
        when(ctx.queryParam("from")).thenReturn("");

        // when
        ZonedDateTime result = Converters.convertZonedDateTime(ctx, "from");

        // then
        assertNull(result);
    }

    @Test
    void convertZonedDateTime_whenDateRangeAvailable_thenReturn() {
        // given
        when(ctx.queryParam("from")).thenReturn("2025-12-05T00:00:00+0100");

        // when
        ZonedDateTime result = Converters.convertZonedDateTime(ctx, "from");

        // then
        assertEquals("2025-12-05T00:00+01:00", result.toString());
    }
}
