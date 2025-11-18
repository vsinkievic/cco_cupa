package lt.creditco.cupa.base.users;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.security.AuthoritiesConstants;

@Entity
@DiscriminatorValue("CUPA_USER")
public class CupaUser extends com.bpmid.vapp.domain.User {

    @Getter
    @Setter
    @Column(name = "merchant_ids", length = 512)
    private String merchantIds;


    public boolean canAccessEntity(MerchantOwnedEntity entity) {
        if (entity == null) {
            return false;
        }

        // Admin users can access all entities
        if (hasAuthority(AuthoritiesConstants.ADMIN)) {
            return true;
        }

        // Regular users can only access entities of their assigned merchants
        String entityMerchantId = entity.getMerchantId();
        if (entityMerchantId == null) {
            return false;
        }

        return getMerchantIdsSet().contains(entityMerchantId);
    }

    public Set<String> getMerchantIdsSet() {
        if (merchantIds == null || merchantIds.trim().isEmpty()) {
            return Set.of();
        }

        return Arrays.stream(merchantIds.split(",")).map(String::trim).filter(id -> !id.isEmpty()).collect(Collectors.toSet());
    }

    /**
     * Check if user has access to all merchants (ADMIN or CREDITCO roles).
     * This centralizes the access control logic in one place.
     * 
     * @return true if user is ADMIN or CREDITCO, false otherwise
     */
    public boolean hasAccessToAllMerchants() {
        return hasAuthority(AuthoritiesConstants.ADMIN) || 
               hasAuthority(AuthoritiesConstants.CREDITCO);
    }

}
