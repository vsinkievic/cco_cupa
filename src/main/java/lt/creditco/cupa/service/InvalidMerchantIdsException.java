package lt.creditco.cupa.service;

/**
 * Exception thrown when invalid merchant IDs are provided.
 */
public class InvalidMerchantIdsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidMerchantIdsException(String message) {
        super(message);
    }

    public InvalidMerchantIdsException(String message, Throwable cause) {
        super(message, cause);
    }
}
