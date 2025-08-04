package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import lt.creditco.cupa.domain.ClientCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ClientCard entity.
 */
@Repository
public interface ClientCardRepository extends JpaRepository<ClientCard, Long> {
    default Optional<ClientCard> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ClientCard> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ClientCard> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select clientCard from ClientCard clientCard left join fetch clientCard.client",
        countQuery = "select count(clientCard) from ClientCard clientCard"
    )
    Page<ClientCard> findAllWithToOneRelationships(Pageable pageable);

    @Query("select clientCard from ClientCard clientCard left join fetch clientCard.client")
    List<ClientCard> findAllWithToOneRelationships();

    @Query("select clientCard from ClientCard clientCard left join fetch clientCard.client where clientCard.id =:id")
    Optional<ClientCard> findOneWithToOneRelationships(@Param("id") Long id);
}
