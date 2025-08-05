package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AuditLog} and its DTO {@link AuditLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends EntityMapper<AuditLogDTO, AuditLog> {
    AuditLogDTO toDto(AuditLog s);
}
