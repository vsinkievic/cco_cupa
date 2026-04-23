package lt.creditco.cupa.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Resolves "from" / "to" {@link LocalDate} pairs for the payment transaction list. Uses the
 * current user's time zone; week boundaries use {@code firstDayOfWeekVaadin} (Vaadin: 0 = Sunday …
 * 6 = Saturday) from {@code UserService.getCurrentUserFirstDayOfWeek()}.
 * <p>
 * For rolling windows (e.g. "this week", default preset), the "to" date is typically
 * <strong>tomorrow</strong> in that zone (see plan), so the half-open instant range can include
 * all of today and buffer edge timestamps.
 */
public final class PaymentTransactionListDatePresets {

    private PaymentTransactionListDatePresets() {}

    public static LocalDate[] resolveLocalDates(
        PaymentTransactionListDatePreset period,
        ZoneId zone,
        int firstDayOfWeekVaadin
    ) {
        return resolveLocalDates(period, firstDayOfWeekVaadin, LocalDate.now(zone));
    }

    /**
     * @param today "today" in the user's time zone (fixed in unit tests; production path uses
     *     {@link #resolveLocalDates(PaymentTransactionListDatePreset, ZoneId, int)} with {@link LocalDate#now(ZoneId)}).
     */
    public static LocalDate[] resolveLocalDates(
        PaymentTransactionListDatePreset period,
        int firstDayOfWeekVaadin,
        LocalDate today
    ) {
        if (period == null) {
            throw new IllegalArgumentException("period must not be null");
        }
        DayOfWeek weekStart = vaadinFirstDayToDayOfWeek(firstDayOfWeekVaadin);
        LocalDate tomorrow = today.plusDays(1);

        return switch (period) {
            case CUSTOM -> throw new IllegalStateException("No preset range for custom; set dates from the pickers");
            case TODAY -> new LocalDate[] { today, today };
            case THIS_WEEK -> {
                LocalDate start = today.with(TemporalAdjusters.previousOrSame(weekStart));
                // Rolling window: week start through end of "tomorrow" in half-open instant terms
                yield new LocalDate[] { start, tomorrow };
            }
            case LAST_WEEK -> {
                LocalDate thisWeekStart = today.with(TemporalAdjusters.previousOrSame(weekStart));
                LocalDate start = thisWeekStart.minusDays(7);
                LocalDate end = start.plusDays(6);
                yield new LocalDate[] { start, end };
            }
            case THIS_MONTH -> {
                LocalDate start = today.with(TemporalAdjusters.firstDayOfMonth());
                yield new LocalDate[] { start, tomorrow };
            }
            case LAST_MONTH -> {
                LocalDate firstThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate start = firstThisMonth.minusMonths(1);
                LocalDate end = firstThisMonth.minusDays(1);
                yield new LocalDate[] { start, end };
            }
            case SINCE_START_PREV_MONTH -> {
                LocalDate start = today.with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
                yield new LocalDate[] { start, tomorrow };
            }
            case THIS_YEAR -> {
                LocalDate start = today.with(TemporalAdjusters.firstDayOfYear());
                yield new LocalDate[] { start, tomorrow };
            }
            case LAST_YEAR -> {
                LocalDate start = today.with(TemporalAdjusters.firstDayOfYear()).minusYears(1);
                LocalDate end = today.with(TemporalAdjusters.firstDayOfYear()).minusDays(1);
                yield new LocalDate[] { start, end };
            }
        };
    }

    private static DayOfWeek vaadinFirstDayToDayOfWeek(int vaadin0to6) {
        if (vaadin0to6 < 0 || vaadin0to6 > 6) {
            return DayOfWeek.MONDAY;
        }
        return DayOfWeek.SUNDAY.plus(vaadin0to6);
    }
}
