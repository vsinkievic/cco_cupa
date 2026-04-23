package lt.creditco.cupa.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Converts an inclusive [from, to] local-date range in a zone to a half-open
 * {@code [startInclusive, endExclusive)} on the {@link Instant} timeline for storage queries.
 * <ul>
 *   <li>Start: beginning of the {@code from} day in the user zone.
 *   <li>End (exclusive): beginning of the day after {@code to} in the user zone, so the whole {@code to} day is included.
 * </ul>
 */
public final class UserLocalDateRange {

    private UserLocalDateRange() {}

    public static Instant startInclusiveAtZoneStartOfDay(LocalDate from, ZoneId zone) {
        return from.atStartOfDay(zone).toInstant();
    }

    public static Instant endExclusiveAfterToDate(LocalDate to, ZoneId zone) {
        return to.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
