package lt.creditco.cupa.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.ZoneId;
import lt.creditco.cupa.util.UserLocalDateRange;
import org.junit.jupiter.api.Test;

class PaymentTransactionListDatePresetsTest {

    private static final ZoneId V = ZoneId.of("Europe/Vilnius");

    /**
     * 2024-06-12 is a Wednesday. Monday as first day of week (Vaadin: 1) → this week starts 2024-06-10.
     */
    @Test
    void thisWeekUsesWeekStartAndTomorrowAsTo() {
        LocalDate today = LocalDate.of(2024, 6, 12);
        LocalDate[] r = PaymentTransactionListDatePresets.resolveLocalDates(
            PaymentTransactionListDatePreset.THIS_WEEK,
            1,
            today
        );
        assertThat(r[0]).isEqualTo(LocalDate.of(2024, 6, 10));
        assertThat(r[1]).isEqualTo(today.plusDays(1));
    }

    @Test
    void defaultPresetSinceStartOfPreviousMonth() {
        LocalDate today = LocalDate.of(2024, 6, 12);
        LocalDate[] r = PaymentTransactionListDatePresets.resolveLocalDates(
            PaymentTransactionListDatePreset.SINCE_START_PREV_MONTH,
            1,
            today
        );
        assertThat(r[0]).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(r[1]).isEqualTo(today.plusDays(1));
    }

    @Test
    void lastMonthIsFullPreviousCalendarMonth() {
        LocalDate today = LocalDate.of(2024, 6, 12);
        LocalDate[] r = PaymentTransactionListDatePresets.resolveLocalDates(
            PaymentTransactionListDatePreset.LAST_MONTH,
            1,
            today
        );
        assertThat(r[0]).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(r[1]).isEqualTo(LocalDate.of(2024, 5, 31));
    }

    @Test
    void customThrows() {
        assertThatThrownBy(
            () -> PaymentTransactionListDatePresets.resolveLocalDates(
                PaymentTransactionListDatePreset.CUSTOM,
                1,
                LocalDate.now(V)
            )
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void halfOpenInstantsForDefaultPresetAndVilnius() {
        LocalDate today = LocalDate.of(2024, 6, 12);
        LocalDate[] r = PaymentTransactionListDatePresets.resolveLocalDates(
            PaymentTransactionListDatePreset.SINCE_START_PREV_MONTH,
            1,
            today
        );
        var start = UserLocalDateRange.startInclusiveAtZoneStartOfDay(r[0], V);
        var end = UserLocalDateRange.endExclusiveAfterToDate(r[1], V);
        assertThat(start).isEqualTo(LocalDate.of(2024, 5, 1).atStartOfDay(V).toInstant());
        assertThat(end).isEqualTo(r[1].plusDays(1).atStartOfDay(V).toInstant());
        assertThat(start).isBefore(end);
    }
}
