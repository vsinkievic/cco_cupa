package lt.creditco.cupa.web.rest;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.repository.UserRepository;
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

    private final UserRepository userRepository;

    public AuditLogResource(AuditLogService auditLogService, AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get the current authenticated user from the principal.
     *
     * @param principal the authenticated principal
     * @return the current user, or null if anonymous
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return null;
        }

        // Try to find user by login (principal.getName()) with authorities eagerly loaded
        Optional<User> userOpt = userRepository.findOneWithAuthoritiesByLogin(principal.getName());
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        LOG.warn("User not found for principal: {} - returning empty results", principal.getName());
        return null;
    }

    /**
     * {@code GET  /audit-logs} : get all the auditLogs.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditLogs in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AuditLogDTO>> getAllAuditLogs(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        Principal principal
    ) {
        LOG.debug("REST request to get a page of AuditLogs");
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return empty page for anonymous users
            Page<AuditLogDTO> emptyPage = Page.empty(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), emptyPage);
            return ResponseEntity.ok().headers(headers).body(emptyPage.getContent());
        }

        Page<AuditLogDTO> page;
        if (eagerload) {
            page = auditLogService.findAllWithEagerRelationshipsWithAccessControl(pageable, currentUser);
        } else {
            page = auditLogService.findAllWithAccessControl(pageable, currentUser);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /audit-logs/:id} : get the "id" auditLog.
     *
     * @param id the id of the auditLogDTO to retrieve.
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditLogDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable("id") Long id, Principal principal) {
        LOG.debug("REST request to get AuditLog : {}", id);
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return 404 for anonymous users
            return ResponseEntity.notFound().build();
        }

        Optional<AuditLogDTO> auditLogDTO = auditLogService.findOneWithAccessControl(id, currentUser);
        return ResponseUtil.wrapOrNotFound(auditLogDTO);
    }
}
