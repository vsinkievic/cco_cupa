package lt.creditco.cupa.service;

import java.util.Optional;
import java.util.Set;

import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.ClientCard;
import com.bpmid.vapp.domain.User;
import lt.creditco.cupa.repository.ClientCardRepository;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.mapper.ClientCardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.ClientCard}.
 */
@Service
@Transactional
public class ClientCardService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCardService.class);

    private final ClientCardRepository clientCardRepository;

    private final ClientCardMapper clientCardMapper;

    public ClientCardService(ClientCardRepository clientCardRepository, ClientCardMapper clientCardMapper) {
        this.clientCardRepository = clientCardRepository;
        this.clientCardMapper = clientCardMapper;
    }

    /**
     * Save a clientCard.
     *
     * @param clientCardDTO the entity to save.
     * @return the persisted entity.
     */
    public ClientCardDTO save(ClientCardDTO clientCardDTO) {
        LOG.debug("Request to save ClientCard : {}", clientCardDTO);
        ClientCard clientCard = clientCardMapper.toEntity(clientCardDTO);
        clientCard = clientCardRepository.save(clientCard);
        return clientCardMapper.toDto(clientCard);
    }

    /**
     * Update a clientCard.
     *
     * @param clientCardDTO the entity to save.
     * @return the persisted entity.
     */
    public ClientCardDTO update(ClientCardDTO clientCardDTO) {
        LOG.debug("Request to update ClientCard : {}", clientCardDTO);
        ClientCard clientCard = clientCardMapper.toEntity(clientCardDTO);
        clientCard = clientCardRepository.save(clientCard);
        return clientCardMapper.toDto(clientCard);
    }

    /**
     * Partially update a clientCard.
     *
     * @param clientCardDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ClientCardDTO> partialUpdate(ClientCardDTO clientCardDTO) {
        LOG.debug("Request to partially update ClientCard : {}", clientCardDTO);

        return clientCardRepository
            .findById(clientCardDTO.getId())
            .map(existingClientCard -> {
                clientCardMapper.partialUpdate(existingClientCard, clientCardDTO);

                return existingClientCard;
            })
            .map(clientCardRepository::save)
            .map(clientCardMapper::toDto);
    }


    /**
     * Get all the clientCards with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ClientCardDTO> findAllWithEagerRelationships(Pageable pageable) {
        return clientCardRepository.findAllWithEagerRelationships(pageable).map(clientCardMapper::toDto);
    }


    /**
     * Delete the clientCard by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete ClientCard : {}", id);
        clientCardRepository.deleteById(id);
    }

    /**
     * Get the count of all client cards.
     *
     * @return the count of entities.
     */
    @Transactional(readOnly = true)
    public long count() {
        LOG.debug("Request to count all ClientCards");
        return clientCardRepository.count();
    }

    /**
     * Get all the client cards with access control based on user's merchant access.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    @Transactional(readOnly = true)
    public Page<ClientCardDTO> findAllWithAccessControl(Pageable pageable, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return Page.empty(pageable);
        }

        LOG.debug("Request to get all ClientCards with access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return clientCardRepository.findAll(pageable).map(clientCardMapper::toDto);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return clientCardRepository.findAllByMerchantIds(merchantIds, pageable).map(clientCardMapper::toDto);
        } else return Page.empty(pageable);
    }

    /**
     * Get all the client cards with eager load of many-to-many relationships and access control.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    public Page<ClientCardDTO> findAllWithEagerRelationshipsWithAccessControl(Pageable pageable, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return Page.empty(pageable);
        }

        LOG.debug("Request to get all ClientCards with eager relationships and access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findAllWithEagerRelationships(pageable);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return clientCardRepository.findAllByMerchantIds(merchantIds, pageable).map(clientCardMapper::toDto);
        } else return Page.empty(pageable);
    }

    /**
     * Get the "id" client card with access control.
     *
     * @param id the id of the entity.
     * @param user the authenticated user.
     * @return the entity if accessible.
     */
    @Transactional(readOnly = true)
    public Optional<ClientCardDTO> findOneWithAccessControl(String id, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt for ClientCard ID: {} - returning empty result", id);
            return Optional.empty();
        }

        LOG.debug("Request to get ClientCard : {} with access control for user: {}", id, user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return clientCardRepository.findById(id).map(clientCardMapper::toDto);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Optional.empty();
            }
            return clientCardRepository.findByIdAndMerchantIds(id, merchantIds).map(clientCardMapper::toDto);
        } else return Optional.empty();
    }
}
