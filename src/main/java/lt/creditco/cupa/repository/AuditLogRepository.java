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

    // Projection methods for distinct values
    @Query("SELECT DISTINCT a.httpMethod FROM AuditLog a ORDER BY a.httpMethod")
    List<String> findDistinctHttpMethods();

    @Query("SELECT DISTINCT a.httpStatusCode FROM AuditLog a ORDER BY a.httpStatusCode")
    List<Integer> findDistinctHttpStatusCodes();

    /**
     * Find audit logs by filters.
     * Service layer handles access control - repository just filters data.
     * 
     * @param merchantIds Merchant IDs to filter by (null = all merchants)
     * @param endpointPattern Filter by endpoint fragment (LIKE pattern)
     * @param method Filter by exact HTTP method
     * @param orderIdPattern Filter by order ID fragment (LIKE pattern)
     * @param environment Filter by environment (TEST or LIVE)
     * @param statusCodes Filter by HTTP status codes (can be multiple)
     * @param pageable Pagination and sorting
     * @return Page of filtered audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
       "(:merchantIds IS NULL OR a.merchantId IN :merchantIds) AND " +
       "(:endpointPattern IS NULL OR a.apiEndpoint LIKE :endpointPattern) AND " +
       "(:method IS NULL OR a.httpMethod = :method) AND " +
       "(:orderIdPattern IS NULL OR a.orderId LIKE :orderIdPattern) AND " +
       "(:environment IS NULL OR a.environment = :environment) AND " +
       "(:statusCodes IS NULL OR a.httpStatusCode IN :statusCodes)")
    Page<AuditLog> findByFilters(
        @Param("merchantIds") List<String> merchantIds,
        @Param("endpointPattern") String endpointPattern,
        @Param("method") String method,
        @Param("orderIdPattern") String orderIdPattern,
        @Param("environment") String environment,
        @Param("statusCodes") List<Integer> statusCodes,
        Pageable pageable
    );
}
