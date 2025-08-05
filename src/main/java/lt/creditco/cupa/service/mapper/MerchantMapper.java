package lt.creditco.cupa.service.mapper;

import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Merchant} and its DTO {@link MerchantDTO}.
 */
@Mapper(componentModel = "spring")
public interface MerchantMapper extends EntityMapper<MerchantDTO, Merchant> {
    @Override
    Merchant toEntity(MerchantDTO dto);
}
