package lt.creditco.cupa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Embeddable class representing a daily monetary transaction limit with linear interpolation.
 * 
 * <p>The limit can be configured in three ways:</p>
 * <ul>
 *   <li>No limit: all fields are null</li>
 *   <li>Simple limit: only afterDate and afterAmount are set (starts from 0 on afterDate)</li>
 *   <li>Linear interpolation: all fields are set (limit gradually increases from startDate to afterDate)</li>
 * </ul>
 * 
 * <p>The limit represents the maximum total monetary amount of transactions that can be processed per day.</p>
 * 
 * <p>Future enhancement: The {@code calculateLimitForDate(LocalDate date)} method will calculate
 * the effective limit for a specific date using linear interpolation between startDate and afterDate.</p>
 */
@Embeddable
public class DailyAmountLimit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "limit_start_date")
    private LocalDate startDate;

    @Column(name = "limit_start_amount", precision = 21, scale = 2)
    private BigDecimal startAmount;

    @Column(name = "limit_after_date")
    private LocalDate afterDate;

    @Column(name = "limit_after_amount", precision = 21, scale = 2)
    private BigDecimal afterAmount;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getStartAmount() {
        return startAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        this.startAmount = startAmount;
    }

    public LocalDate getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(LocalDate afterDate) {
        this.afterDate = afterDate;
    }

    public BigDecimal getAfterAmount() {
        return afterAmount;
    }

    public void setAfterAmount(BigDecimal afterAmount) {
        this.afterAmount = afterAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyAmountLimit that = (DailyAmountLimit) o;
        return Objects.equals(startDate, that.startDate) &&
               Objects.equals(startAmount, that.startAmount) &&
               Objects.equals(afterDate, that.afterDate) &&
               Objects.equals(afterAmount, that.afterAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, startAmount, afterDate, afterAmount);
    }

    @Override
    public String toString() {
        return "DailyAmountLimit{" +
            "startDate=" + startDate +
            ", startAmount=" + startAmount +
            ", afterDate=" + afterDate +
            ", afterAmount=" + afterAmount +
            '}';
    }

    /**
     * Validates the daily amount limit configuration.
     *
     * @param environment the environment context (e.g., "TEST", "LIVE") for error messages
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public void validate(String environment) {
        boolean hasStartDate = startDate != null;
        boolean hasStartAmount = startAmount != null;
        boolean hasAfterDate = afterDate != null;
        boolean hasAfterAmount = afterAmount != null;

        // If all fields are null, it's valid (no limit)
        if (!hasStartDate && !hasStartAmount && !hasAfterDate && !hasAfterAmount) {
            return;
        }

        // Validate date-amount pairing for start
        if (hasStartDate && !hasStartAmount) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: start date is set but start amount is missing");
        }
        if (hasStartAmount && !hasStartDate) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: start amount is set but start date is missing");
        }

        // Validate date-amount pairing for after
        if (hasAfterDate && !hasAfterAmount) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: after date is set but after amount is missing");
        }
        if (hasAfterAmount && !hasAfterDate) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: after amount is set but after date is missing");
        }

        // If start is configured, after must also be configured
        if ((hasStartDate || hasStartAmount) && (!hasAfterDate || !hasAfterAmount)) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: if start date/amount is set, after date/amount must also be set");
        }

        // Validate amounts are >= 0
        if (hasStartAmount && startAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: start amount must be greater than or equal to 0");
        }
        if (hasAfterAmount && afterAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: after amount must be greater than or equal to 0");
        }

        // If both dates are set, validate afterDate is after startDate
        if (hasStartDate && hasAfterDate && !afterDate.isAfter(startDate)) {
            throw new IllegalArgumentException(
                environment + " daily amount limit: after date must be after start date");
        }
    }

    /**
     * Future method to calculate the effective daily limit for a specific date.
     * 
     * <p>Implementation will use linear interpolation:</p>
     * <ul>
     *   <li>Before startDate: return 0 (or no limit)</li>
     *   <li>Between startDate and afterDate: interpolate linearly</li>
     *   <li>On or after afterDate: return afterAmount</li>
     * </ul>
     * 
     * @param date the date for which to calculate the limit
     * @return the effective daily monetary limit for the given date, or null if no limit configured
     */
    public BigDecimal getLimitForDate(LocalDate date) {

        if (date == null) return BigDecimal.ZERO;
        if (startDate == null && afterDate == null) return afterAmount == null ? BigDecimal.ZERO : afterAmount;

        if (startDate != null && date.isBefore(startDate)) return BigDecimal.ZERO;
        if (startDate == null && afterDate != null && date.isBefore(afterDate)) return BigDecimal.ZERO;

        if (afterDate != null && !date.isBefore(afterDate)) return afterAmount;

        int dayNumber = startDate.until(date).getDays();
        int totalDays = startDate.until(afterDate).getDays();

        BigDecimal proportionalAmount = startAmount.add(afterAmount.subtract(startAmount).multiply(BigDecimal.valueOf(dayNumber)).divide(BigDecimal.valueOf(totalDays), 20, RoundingMode.HALF_UP));

        return proportionalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isLimitExceeded(BigDecimal transactionAmount, BigDecimal totalForDay, LocalDate date){
        BigDecimal dayTurnover = transactionAmount == null ? BigDecimal.ZERO : transactionAmount;
        dayTurnover = dayTurnover.add(totalForDay == null ? BigDecimal.ZERO : totalForDay);
        BigDecimal limit = getLimitForDate(date);

        return dayTurnover.compareTo(limit) > 0;
    }
}

