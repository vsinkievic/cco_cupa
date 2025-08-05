package lt.creditco.cupa.service;

import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.domain.ClientCard;
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
     * Get all the clientCards.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ClientCardDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ClientCards");
        return clientCardRepository.findAll(pageable).map(clientCardMapper::toDto);
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
     * Get one clientCard by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ClientCardDTO> findOne(String id) {
        LOG.debug("Request to get ClientCard : {}", id);
        return clientCardRepository.findOneWithEagerRelationships(id).map(clientCardMapper::toDto);
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
}
