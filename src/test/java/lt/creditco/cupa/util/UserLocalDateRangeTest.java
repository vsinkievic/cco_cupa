package lt.creditco.cupa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class UserLocalDateRangeTest {

    @Test
    void halfOpenRangeInZoneMatchesStartOfFromAndStartOfDayAfterTo() {
        ZoneId v = ZoneId.of("Europe/Vilnius");
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 2);

        Instant start = UserLocalDateRange.startInclusiveAtZoneStartOfDay(from, v);
        Instant end = UserLocalDateRange.endExclusiveAfterToDate(to, v);

        assertThat(start).isEqualTo(from.atStartOfDay(v).toInstant());
        assertThat(end).isEqualTo(to.plusDays(1).atStartOfDay(v).toInstant());
        assertThat(start).isBefore(end);
    }
}
