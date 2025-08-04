package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Client} and its DTO {@link ClientDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper extends EntityMapper<ClientDTO, Client> {
    @Mapping(target = "merchant", source = "merchant", qualifiedByName = "merchantName")
    ClientDTO toDto(Client s);

    @Named("merchantName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    MerchantDTO toDtoMerchantName(Merchant merchant);
}
