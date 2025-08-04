package lt.creditco.cupa.service;

import java.util.Optional;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.Merchant}.
 */
@Service
@Transactional
public class MerchantService {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository merchantRepository;

    private final MerchantMapper merchantMapper;

    public MerchantService(MerchantRepository merchantRepository, MerchantMapper merchantMapper) {
        this.merchantRepository = merchantRepository;
        this.merchantMapper = merchantMapper;
    }

    /**
     * Save a merchant.
     *
     * @param merchantDTO the entity to save.
     * @return the persisted entity.
     */
    public MerchantDTO save(MerchantDTO merchantDTO) {
        LOG.debug("Request to save Merchant : {}", merchantDTO);
        Merchant merchant = merchantMapper.toEntity(merchantDTO);
        merchant = merchantRepository.save(merchant);
        return merchantMapper.toDto(merchant);
    }

    /**
     * Update a merchant.
     *
     * @param merchantDTO the entity to save.
     * @return the persisted entity.
     */
    public MerchantDTO update(MerchantDTO merchantDTO) {
        LOG.debug("Request to update Merchant : {}", merchantDTO);
        Merchant merchant = merchantMapper.toEntity(merchantDTO);
        merchant = merchantRepository.save(merchant);
        return merchantMapper.toDto(merchant);
    }

    /**
     * Partially update a merchant.
     *
     * @param merchantDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MerchantDTO> partialUpdate(MerchantDTO merchantDTO) {
        LOG.debug("Request to partially update Merchant : {}", merchantDTO);

        return merchantRepository
            .findById(merchantDTO.getId())
            .map(existingMerchant -> {
                merchantMapper.partialUpdate(existingMerchant, merchantDTO);

                return existingMerchant;
            })
            .map(merchantRepository::save)
            .map(merchantMapper::toDto);
    }

    /**
     * Get all the merchants.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<MerchantDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Merchants");
        return merchantRepository.findAll(pageable).map(merchantMapper::toDto);
    }

    /**
     * Get one merchant by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MerchantDTO> findOne(Long id) {
        LOG.debug("Request to get Merchant : {}", id);
        return merchantRepository.findById(id).map(merchantMapper::toDto);
    }

    /**
     * Delete the merchant by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Merchant : {}", id);
        merchantRepository.deleteById(id);
    }
}
