package lt.creditco.cupa.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Query(
        value = "select p from PaymentTransaction p where p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive",
        countQuery = "select count(p) from PaymentTransaction p where p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive"
    )
    Page<PaymentTransaction> findAllByRequestTimestampRange(
        @Param("start") Instant start,
        @Param("endExclusive") Instant endExclusive,
        Pageable pageable
    );

    @Transactional(readOnly = true)
    @Query(
        value = "select p from PaymentTransaction p where p.merchantId in :merchantIds and p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive",
        countQuery = "select count(p) from PaymentTransaction p where p.merchantId in :merchantIds and p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive"
    )
    Page<PaymentTransaction> findAllByMerchantIdsAndRequestTimestampRange(
        @Param("merchantIds") Set<String> merchantIds,
        @Param("start") Instant start,
        @Param("endExclusive") Instant endExclusive,
        Pageable pageable
    );

    /**
     * Same time range as {@link #findAllByRequestTimestampRange} but returns a limited list without a COUNT query.
     */
    @Transactional(readOnly = true)
    @Query("select p from PaymentTransaction p where p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive")
    List<PaymentTransaction> findListByRequestTimestampRange(
        @Param("start") Instant start,
        @Param("endExclusive") Instant endExclusive,
        Pageable pageable
    );

    /**
     * Same time range as {@link #findAllByMerchantIdsAndRequestTimestampRange} but returns a limited list without a COUNT query.
     */
    @Transactional(readOnly = true)
    @Query(
        "select p from PaymentTransaction p where p.merchantId in :merchantIds and p.requestTimestamp >= :start and p.requestTimestamp < :endExclusive"
    )
    List<PaymentTransaction> findListByMerchantIdsAndRequestTimestampRange(
        @Param("merchantIds") Set<String> merchantIds,
        @Param("start") Instant start,
        @Param("endExclusive") Instant endExclusive,
        Pageable pageable
    );

    @Query("select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.merchantId in :merchantIds")
    List<PaymentTransaction> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds);

    @Query(
        "select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.id = :id and paymentTransaction.merchantId in :merchantIds"
    )
    Optional<PaymentTransaction> findByIdAndMerchantIds(@Param("id") String id, @Param("merchantIds") Set<String> merchantIds);

    @Transactional(readOnly = true)
    @Query(
        "select paymentTransaction from PaymentTransaction paymentTransaction where paymentTransaction.merchantId = :merchantId and paymentTransaction.orderId = :orderId"
    )
    Optional<PaymentTransaction> findByMerchantIdAndOrderId(@Param("merchantId") String merchantId, @Param("orderId") String orderId);

    @Transactional(readOnly = true)
    @Query(
        "select count(paymentTransaction) > 0 from PaymentTransaction paymentTransaction where paymentTransaction.merchantId = :merchantId and paymentTransaction.orderId = :orderId"
    )
    boolean existsByMerchantIdAndOrderId(@Param("merchantId") String merchantId, @Param("orderId") String orderId);

    @Transactional(readOnly = true)
    @Query(
        "select sum(paymentTransaction.amount) from PaymentTransaction paymentTransaction where paymentTransaction.merchantId = :merchantId and paymentTransaction.createdDate >= :startDate and paymentTransaction.createdDate <= :endDate"
    )
    BigDecimal getTotalAmountByMerchantIdAndEnvironmentAndDateRange(@Param("merchantId") String merchantId, @Param("environment") String environment, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Transactional(readOnly = true)
    @Query(
        "select count(p) from PaymentTransaction p where p.environment = :environment and p.gatewayMerchantId = :gatewayMerchantId and p.clientEmail = :clientEmail and p.requestTimestamp >= :after"
    )
    int countByEnvironmentAndGatewayMerchantIdAndClientEmailAndAfterRequestTimestamp(
        @Param("environment") MerchantMode environment,
        @Param("gatewayMerchantId") String gatewayMerchantId,
        @Param("clientEmail") String clientEmail,
        @Param("after") Instant after
    );
}
