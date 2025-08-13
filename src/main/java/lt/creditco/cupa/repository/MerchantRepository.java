package lt.creditco.cupa.repository;

import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.domain.Merchant;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Merchant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {
    /**
     * Find a merchant by CUPA test API key.
     *
     * @param cupaTestApiKey the test API key
     * @return the merchant if found
     */
    Optional<Merchant> findOneByCupaTestApiKey(String cupaTestApiKey);

    /**
     * Find a merchant by CUPA production API key.
     *
     * @param cupaProdApiKey the production API key
     * @return the merchant if found
     */
    Optional<Merchant> findOneByCupaProdApiKey(String cupaProdApiKey);
}
