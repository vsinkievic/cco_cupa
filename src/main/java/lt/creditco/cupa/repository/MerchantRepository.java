package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lt.creditco.cupa.domain.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find a merchant by remote test merchant ID.
     *
     * @param remoteTestMerchantId the remote test merchant ID
     * @return the merchant if found
     */
    Optional<Merchant> findByRemoteTestMerchantId(String remoteTestMerchantId);

    /**
     * Find a merchant by remote production merchant ID.
     *
     * @param remoteProdMerchantId the remote production merchant ID
     * @return the merchant if found
     */
    Optional<Merchant> findByRemoteProdMerchantId(String remoteProdMerchantId);

    @Query("select merchant from Merchant merchant where merchant.id =:id")
    Optional<Merchant> findOneWithToOneRelationships(@Param("id") String id);

    @Query("select merchant from Merchant merchant where merchant.id in :merchantIds")
    Page<Merchant> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds, Pageable pageable);

    @Query("select merchant from Merchant merchant where merchant.id in :merchantIds")
    List<Merchant> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds);

    @Query("select merchant from Merchant merchant where merchant.id = :id and merchant.id in :merchantIds")
    Optional<Merchant> findByIdAndMerchantIds(@Param("id") String id, @Param("merchantIds") Set<String> merchantIds);
}
