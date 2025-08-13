package lt.creditco.cupa.domain;

/**
 * Interface for entities that belong to merchants and require access control.
 * All entities that need merchant-based access control should implement this interface.
 */
public interface MerchantOwnedEntity {
    /**
     * Get the merchant ID that owns this entity.
     *
     * @return the merchant ID, or null if not assigned
     */
    String getMerchantId();
}
