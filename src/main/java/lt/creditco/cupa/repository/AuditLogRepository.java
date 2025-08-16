package lt.creditco.cupa.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lt.creditco.cupa.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    default Optional<AuditLog> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AuditLog> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AuditLog> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select auditLog from AuditLog auditLog", countQuery = "select count(auditLog) from AuditLog auditLog")
    Page<AuditLog> findAllWithToOneRelationships(Pageable pageable);

    @Query("select auditLog from AuditLog auditLog")
    List<AuditLog> findAllWithToOneRelationships();

    @Query("select auditLog from AuditLog auditLog where auditLog.id =:id")
    Optional<AuditLog> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("select auditLog from AuditLog auditLog where auditLog.merchantId in :merchantIds")
    Page<AuditLog> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds, Pageable pageable);

    @Query("select auditLog from AuditLog auditLog where auditLog.merchantId in :merchantIds")
    List<AuditLog> findAllByMerchantIds(@Param("merchantIds") Set<String> merchantIds);

    @Query("select auditLog from AuditLog auditLog where auditLog.id = :id and auditLog.merchantId in :merchantIds")
    Optional<AuditLog> findByIdAndMerchantIds(@Param("id") Long id, @Param("merchantIds") Set<String> merchantIds);
}
