package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import lt.creditco.cupa.domain.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PaymentTransaction entity.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    default Optional<PaymentTransaction> findOneWithEagerRelationships(String id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<PaymentTransaction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<PaymentTransaction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select paymentTransaction from PaymentTransaction paymentTransaction",
        countQuery = "select count(paymentTransaction) from PaymentTransaction paymentTransaction"
    )
    Page<PaymentTransaction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select paymentTransaction from PaymentTransaction paymentTransaction")
    List<PaymentTransaction> findAllWithToOneRelationships();

    @Query("select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.id =:id")
    Optional<PaymentTransaction> findOneWithToOneRelationships(@Param("id") String id);
}
