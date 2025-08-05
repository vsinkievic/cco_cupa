package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PaymentTransaction} and its DTO {@link PaymentTransactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper extends EntityMapper<PaymentTransactionDTO, PaymentTransaction> {}
