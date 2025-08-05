package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.service.dto.ClientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Client} and its DTO {@link ClientDTO}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClientMapper extends EntityMapper<ClientDTO, Client> {
    @Override
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "removeCard", ignore = true)
    Client toEntity(ClientDTO dto);

    @Override
    @Mapping(target = "merchantName", ignore = true)
    ClientDTO toDto(Client entity);
}
