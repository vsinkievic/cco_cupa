package lt.creditco.cupa.repository;

import java.util.UUID;
import lt.creditco.cupa.domain.Merchant;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Merchant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, String> {}
