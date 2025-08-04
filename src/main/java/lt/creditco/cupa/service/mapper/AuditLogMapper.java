package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AuditLog} and its DTO {@link AuditLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends EntityMapper<AuditLogDTO, AuditLog> {
    @Mapping(target = "merchant", source = "merchant", qualifiedByName = "merchantName")
    AuditLogDTO toDto(AuditLog s);

    @Named("merchantName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    MerchantDTO toDtoMerchantName(Merchant merchant);
}
