package lt.creditco.cupa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.service.mapper.AuditLogMapper;
import lt.creditco.cupa.web.filter.HttpLoggingFilter.ApiRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.AuditLog}.
 */
@Service
@Transactional
public class AuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Save a auditLog.
     *
     * @param auditLogDTO the entity to save.
     * @return the persisted entity.
     */
    public AuditLogDTO save(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to save AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    /**
     * Update a auditLog.
     *
     * @param auditLogDTO the entity to save.
     * @return the persisted entity.
     */
    public AuditLogDTO update(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to update AuditLog : {}", auditLogDTO);
        AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
        auditLog = auditLogRepository.save(auditLog);
        return auditLogMapper.toDto(auditLog);
    }

    /**
     * Partially update a auditLog.
     *
     * @param auditLogDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AuditLogDTO> partialUpdate(AuditLogDTO auditLogDTO) {
        LOG.debug("Request to partially update AuditLog : {}", auditLogDTO);

        return auditLogRepository
            .findById(auditLogDTO.getId())
            .map(existingAuditLog -> {
                auditLogMapper.partialUpdate(existingAuditLog, auditLogDTO);

                return existingAuditLog;
            })
            .map(auditLogRepository::save)
            .map(auditLogMapper::toDto);
    }

    /**
     * Get all the auditLogs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AuditLogs");
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toDto);
    }

    /**
     * Get all the auditLogs with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AuditLogDTO> findAllWithEagerRelationships(Pageable pageable) {
        return auditLogRepository.findAllWithEagerRelationships(pageable).map(auditLogMapper::toDto);
    }

    /**
     * Get one auditLog by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AuditLogDTO> findOne(Long id) {
        LOG.debug("Request to get AuditLog : {}", id);
        return auditLogRepository.findOneWithEagerRelationships(id).map(auditLogMapper::toDto);
    }

    /**
     * Delete the auditLog by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete AuditLog : {}", id);
        auditLogRepository.deleteById(id);
    }

    public void updateAuditLogWithResponse(Long requestId, ApiRequestDetails apiRequestDetails) {
        LOG.debug("Request to update AuditLog with response : {}", requestId);

        AuditLog auditLog = auditLogRepository.findById(requestId).orElse(null);
        if (auditLog != null) {
            auditLog.setRequestData(formatIfJson(apiRequestDetails.getRequestBody()));
            auditLog.setHttpStatusCode(apiRequestDetails.getResponseStatus());
            auditLog.setResponseData(formatIfJson(apiRequestDetails.getResponseBody()));
            auditLog.setResponseDescription(apiRequestDetails.getResponseDescription());
            auditLogRepository.save(auditLog);
        }
    }

    /**
     * Attempts to format JSON string with proper indentation if it's valid JSON.
     */
    private String formatIfJson(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        String trimmed = content.trim();
        // Quick check if it looks like JSON
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try {
                Object json = objectMapper.readValue(trimmed, Object.class);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            } catch (Exception e) {
                // Not valid JSON, return original
                return content;
            }
        }
        return content;
    }
}
