package lt.creditco.cupa.domain.enumeration;

/**
 * Enum for the status of a payment transaction lifecycle.
 * This helps in tracking the state of each payment.
 */
public enum TransactionStatus {
    RECEIVED,
    PENDING,
    ABANDONED,
    AWAITING_CALLBACK,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED,
    QUERY_SUCCESS,
}
