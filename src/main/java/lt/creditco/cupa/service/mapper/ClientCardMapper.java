package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.ClientCard;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ClientCard} and its DTO {@link ClientCardDTO}.
 */
@Mapper(componentModel = "spring")
public interface ClientCardMapper extends EntityMapper<ClientCardDTO, ClientCard> {}
