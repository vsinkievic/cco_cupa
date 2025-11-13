package lt.creditco.cupa.service;

import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.Merchant;
import com.bpmid.vapp.domain.User;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.Merchant}.
 */
@Service
@Transactional
@Slf4j
public class MerchantService {

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
        log.debug("Request to save Merchant : {}", merchantDTO);
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
        log.debug("Request to update Merchant : {}", merchantDTO);
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
        log.debug("Request to partially update Merchant : {}", merchantDTO);

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
        log.debug("Request to get all Merchants");
        return merchantRepository.findAll(pageable).map(merchantMapper::toDto);
    }

    /**
     * Get one merchant by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MerchantDTO> findOne(String id) {
        log.debug("Request to get Merchant : {}", id);
        return merchantRepository.findById(id).map(merchantMapper::toDto);
    }

    /**
     * Delete the merchant by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        log.debug("Request to delete Merchant : {}", id);
        merchantRepository.deleteById(id);
    }

    /**
     * Get the count of all merchants.
     *
     * @return the count of entities.
     */
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Request to count all Merchants");
        return merchantRepository.count();
    }

    /**
     * Find a merchant by CUPA test API key.
     *
     * @param cupaApiKey the test API key
     * @return the merchant if found
     */
    @Transactional(readOnly = true)
    public Merchant findMerchantByCupaApiKey(String cupaApiKey) {
        log.debug("findMerchantByCupaApiKey({})", cupaApiKey);
        return merchantRepository
            .findOneByCupaProdApiKey(cupaApiKey)
            .orElse(merchantRepository.findOneByCupaTestApiKey(cupaApiKey).orElse(null));
    }

    @Transactional(readOnly = true)
    public Merchant findMerchantById(String merchantId) {
        log.debug("findMerchantById({})", merchantId);
        return merchantRepository.findById(merchantId).orElse(null);
    }

    /**
     * Get all the merchants with role-based access control.
     * Admin users get all fields, other users get limited fields (id, name, mode, currency).
     * Non-admin users can only access merchants in their merchant_ids list.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities with role-based field visibility and merchant filtering.
     */
    @Transactional(readOnly = true)
    public Page<MerchantDTO> findAllWithAccessControl(Pageable pageable, User user) {
        if (user == null) {
            log.warn("Anonymous user access attempt - returning empty results");
            return Page.empty(pageable);
        }

        log.debug("Request to get all Merchants with access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            // Admin gets full data
            return findAll(pageable);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Page.empty(pageable);
            }
            Page<Merchant> merchants = merchantRepository.findAllByMerchantIds(merchantIds, pageable);
            return merchants.map(merchant -> mapToLimitedDto(merchant));
        } else return Page.empty(pageable);

    }

    /**
     * Get the "id" merchant with role-based access control.
     * Admin users get all fields, other users get limited fields (id, name, mode, currency).
     * Non-admin users can only access merchants in their merchant_ids list.
     *
     * @param id the id of the entity.
     * @param user the authenticated user.
     * @return the entity with role-based field visibility and merchant filtering.
     */
    @Transactional(readOnly = true)
    public Optional<MerchantDTO> findOneWithAccessControl(String id, User user) {
        if (user == null) {
            log.warn("Anonymous user access attempt for Merchant ID: {} - returning empty result", id);
            return Optional.empty();
        }

        log.debug("Request to get Merchant : {} with access control for user: {}", id, user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            // Admin gets full data
            return findOne(id);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Optional.empty();
            }
            return merchantRepository.findByIdAndMerchantIds(id, merchantIds).map(merchant -> mapToLimitedDto(merchant));
        } else return Optional.empty();
    }

    private MerchantDTO mapToLimitedDto(Merchant merchant) {
        MerchantDTO dto = new MerchantDTO();
        dto.setId(merchant.getId());
        dto.setName(merchant.getName());
        dto.setMode(merchant.getMode());
        dto.setCurrency(merchant.getCurrency());
        // All other fields remain null/default values
        return dto;
    }
}
