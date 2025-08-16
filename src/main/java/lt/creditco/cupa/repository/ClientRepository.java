package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lt.creditco.cupa.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Client entity.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    default Optional<Client> findOneWithEagerRelationships(String id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Client> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Client> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select client from Client client", countQuery = "select count(client) from Client client")
    Page<Client> findAllWithToOneRelationships(Pageable pageable);

    @Query("select client from Client client")
    List<Client> findAllWithToOneRelationships();

    @Query("select client from Client client where client.id =:id")
    Optional<Client> findOneWithToOneRelationships(@Param("id") String id);

    @Query("select client from Client client where client.merchantClientId =:merchantClientId")
    Optional<Client> findByMerchantClientId(@Param("merchantClientId") String merchantClientId);

    @Query("select count(client) > 0 from Client client where client.merchantClientId =:merchantClientId")
    boolean existsByMerchantClientId(@Param("merchantClientId") String merchantClientId);

    @Query("select client from Client client where client.merchantId in :merchantIds")
    Page<Client> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds, Pageable pageable);

    @Query("select client from Client client where client.merchantId in :merchantIds")
    List<Client> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds);

    @Query("select client from Client client where client.id = :id and client.merchantId in :merchantIds")
    Optional<Client> findByIdAndMerchantIds(@Param("id") String id, @Param("merchantIds") Set<String> merchantIds);
}
