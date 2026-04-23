package lt.creditco.cupa.application;

/**
 * Predefined date-range choices for the payment transaction list. Labels are short, clear English
 * (see product plan).
 */
public enum PaymentTransactionListDatePreset {
    CUSTOM("Custom range"),
    TODAY("Today"),
    THIS_WEEK("This week"),
    LAST_WEEK("Last week"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    SINCE_START_PREV_MONTH("Since 1st of previous month"),
    THIS_YEAR("This year"),
    LAST_YEAR("Last year");

    private final String label;

    PaymentTransactionListDatePreset(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PaymentTransactionListDatePreset defaultPeriod() {
        return SINCE_START_PREV_MONTH;
    }
}
