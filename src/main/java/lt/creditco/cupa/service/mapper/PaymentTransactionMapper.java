package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PaymentTransaction} and its DTO {@link PaymentTransactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper extends EntityMapper<PaymentTransactionDTO, PaymentTransaction> {
    @Mapping(target = "client", source = "client", qualifiedByName = "clientMerchantClientId")
    @Mapping(target = "merchant", source = "merchant", qualifiedByName = "merchantName")
    PaymentTransactionDTO toDto(PaymentTransaction s);

    @Named("clientMerchantClientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "merchantClientId", source = "merchantClientId")
    ClientDTO toDtoClientMerchantClientId(Client client);

    @Named("merchantName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    MerchantDTO toDtoMerchantName(Merchant merchant);
}
