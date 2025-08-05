package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.service.dto.ClientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Client} and its DTO {@link ClientDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper extends EntityMapper<ClientDTO, Client> {}
