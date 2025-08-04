package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.ClientCard;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.dto.ClientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClientCard} and its DTO {@link ClientCardDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClientCardMapper extends EntityMapper<ClientCardDTO, ClientCard> {
    @Mapping(target = "client", source = "client", qualifiedByName = "clientMerchantClientId")
    ClientCardDTO toDto(ClientCard s);

    @Named("clientMerchantClientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "merchantClientId", source = "merchantClientId")
    ClientDTO toDtoClientMerchantClientId(Client client);
}
