package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.ClientCard;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link ClientCard} and its DTO {@link ClientCardDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClientCardMapper extends EntityMapper<ClientCardDTO, ClientCard> {
    @Override
    @Mapping(target = "client.cards", ignore = true)
    @Mapping(target = "client.removeCard", ignore = true)
    ClientCard toEntity(ClientCardDTO dto);

    @Override
    @Mapping(target = "client.merchantName", ignore = true)
    ClientCardDTO toDto(ClientCard entity);
}
