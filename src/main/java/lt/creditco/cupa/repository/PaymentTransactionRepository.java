package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Query("select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.merchantId in :merchantIds")
    Page<PaymentTransaction> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds, Pageable pageable);

    @Query("select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.merchantId in :merchantIds")
    List<PaymentTransaction> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds);

    @Query(
        "select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.id = :id and paymentTransaction.merchantId in :merchantIds"
    )
    Optional<PaymentTransaction> findByIdAndMerchantIds(@Param("id") String id, @Param("merchantIds") Set<String> merchantIds);

    @Query(
        "select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.merchantId = :merchantId and paymentTransaction.orderId = :orderId"
    )
    Optional<PaymentTransaction> findByMerchantIdAndOrderId(@Param("merchantId") String merchantId, @Param("orderId") String orderId);

    @Query(
        "select count(paymentTransaction) > 0 from PaymentTransaction paymentTransaction where paymentTransaction.merchantId = :merchantId and paymentTransaction.orderId = :orderId"
    )
    boolean existsByMerchantIdAndOrderId(@Param("merchantId") String merchantId, @Param("orderId") String orderId);
}
