package lt.creditco.cupa.web.rest;

import java.util.List;
import java.util.Optional;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link lt.creditco.cupa.domain.AuditLog}.
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogResource.class);

    private static final String ENTITY_NAME = "auditLog";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuditLogService auditLogService;

    private final AuditLogRepository auditLogRepository;

    public AuditLogResource(AuditLogService auditLogService, AuditLogRepository auditLogRepository) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * {@code GET  /audit-logs} : get all the auditLogs.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditLogs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of AuditLogs");
        Page<AuditLogDTO> page;
        if (eagerload) {
            page = auditLogService.findAllWithEagerRelationships(pageable);
        } else {
            page = auditLogService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /audit-logs/:id} : get the "id" auditLog.
     *
     * @param id the id of the auditLogDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditLogDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AuditLog : {}", id);
        Optional<AuditLogDTO> auditLogDTO = auditLogService.findOne(id);
        return ResponseUtil.wrapOrNotFound(auditLogDTO);
    }
}
