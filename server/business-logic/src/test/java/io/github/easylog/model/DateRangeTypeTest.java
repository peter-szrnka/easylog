package io.github.easylog.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Peter Szrnka
 */
class DateRangeTypeTest {

    @ParameterizedTest
    @EnumSource(DateRangeType.class)
    void from_shouldReturnExpectedZonedDateTime(DateRangeType dateRangeType) {
        // given
        ZonedDateTime input = ZonedDateTime.now().minusDays(10);

        // when
        ZonedDateTime result = DateRangeType.from(dateRangeType, input);

        // then
        if (dateRangeType == DateRangeType.CUSTOM) {
            assertThat(result).isEqualTo(input);
        } else {
            ZonedDateTime now = ZonedDateTime.now();
            switch (dateRangeType) {
                case LAST_5_MINUTES -> assertThat(result).isAfter(now.minusMinutes(6)).isBefore(now.minusMinutes(4));
                case LAST_15_MINUTES -> assertThat(result).isAfter(now.minusMinutes(16)).isBefore(now.minusMinutes(14));
                case LAST_30_MINUTES -> assertThat(result).isAfter(now.minusMinutes(31)).isBefore(now.minusMinutes(29));
                case LAST_1_HOUR -> assertThat(result).isAfter(now.minusHours(1).minusSeconds(1)).isBefore(now.minusHours(1).plusSeconds(1));
                case LAST_4_HOURS -> assertThat(result).isAfter(now.minusHours(4).minusSeconds(1)).isBefore(now.minusHours(4).plusSeconds(1));
                case LAST_1_DAY -> assertThat(result).isAfter(now.minusDays(1).minusSeconds(1)).isBefore(now.minusDays(1).plusSeconds(1));
                case LAST_7_DAYS -> assertThat(result).isAfter(now.minusDays(7).minusSeconds(1)).isBefore(now.minusDays(7).plusSeconds(1));
                case LAST_1_MONTH -> assertThat(result).isAfter(now.minusMonths(1).minusSeconds(1)).isBefore(now.minusMonths(1).plusSeconds(1));
                case LIVE -> assertThat(result).isAfter(now.minusSeconds(1)).isBefore(now.plusSeconds(1));
                default -> assertThat(result).isAfter(now.minusMinutes(16)).isBefore(now.minusMinutes(14));
            }
        }
    }
}