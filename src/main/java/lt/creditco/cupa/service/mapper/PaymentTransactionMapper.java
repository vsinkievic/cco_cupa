package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link PaymentTransaction} and its DTO {@link PaymentTransactionDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PaymentTransactionMapper extends EntityMapper<PaymentTransactionDTO, PaymentTransaction> {
    @Override
    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "merchantName", ignore = true)
    PaymentTransactionDTO toDto(PaymentTransaction entity);
}
