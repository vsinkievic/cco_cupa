package lt.creditco.cupa.service;

import java.util.Optional;
import java.util.Set;

import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.Client;
import com.bpmid.vapp.domain.User;
import lt.creditco.cupa.repository.ClientRepository;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.mapper.ClientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.Client}.
 */
@Service
@Transactional
public class ClientService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    /**
     * Save a client.
     *
     * @param clientDTO the entity to save.
     * @return the persisted entity.
     */
    public ClientDTO save(ClientDTO clientDTO) {
        LOG.debug("Request to save Client : {}", clientDTO);
        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);
        return clientMapper.toDto(client);
    }

    /**
     * Update a client.
     *
     * @param clientDTO the entity to save.
     * @return the persisted entity.
     */
    public ClientDTO update(ClientDTO clientDTO) {
        LOG.debug("Request to update Client : {}", clientDTO);
        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);
        return clientMapper.toDto(client);
    }

    /**
     * Partially update a client.
     *
     * @param clientDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ClientDTO> partialUpdate(ClientDTO clientDTO) {
        LOG.debug("Request to partially update Client : {}", clientDTO);

        return clientRepository
            .findById(clientDTO.getId())
            .map(existingClient -> {
                clientMapper.partialUpdate(existingClient, clientDTO);

                return existingClient;
            })
            .map(clientRepository::save)
            .map(clientMapper::toDto);
    }

    /**
     * Get all the clients.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ClientDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Clients");
        return clientRepository.findAll(pageable).map(clientMapper::toDto);
    }

    /**
     * Get all the clients with access control based on user's merchant access.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    @Transactional(readOnly = true)
    public Page<ClientDTO> findAllWithAccessControl(Pageable pageable, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return Page.empty(pageable);
        }

        LOG.debug("Request to get all Clients with access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findAll(pageable);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return clientRepository.findAllByMerchantIds(merchantIds, pageable).map(clientMapper::toDto);
        } else return Page.empty(pageable);
    }

    /**
     * Get all the clients with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ClientDTO> findAllWithEagerRelationships(Pageable pageable) {
        return clientRepository.findAllWithEagerRelationships(pageable).map(clientMapper::toDto);
    }

    /**
     * Get all the clients with eager load of many-to-many relationships and access control.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    public Page<ClientDTO> findAllWithEagerRelationshipsWithAccessControl(Pageable pageable, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return Page.empty(pageable);
        }

        LOG.debug("Request to get all Clients with eager relationships and access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findAllWithEagerRelationships(pageable);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
            if (merchantIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return clientRepository.findAllByMerchantIds(merchantIds, pageable).map(clientMapper::toDto);
        } else return Page.empty(pageable);
    }

    /**
     * Get one client by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ClientDTO> findOne(String id) {
        LOG.debug("Request to get Client : {}", id);
        return clientRepository.findOneWithEagerRelationships(id).map(clientMapper::toDto);
    }

    /**
     * Delete the client by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete Client : {}", id);
        clientRepository.deleteById(id);
    }

    /**
     * Get the count of all clients.
     *
     * @return the count of entities.
     */
    @Transactional(readOnly = true)
    public long count() {
        LOG.debug("Request to count all Clients");
        return clientRepository.count();
    }

    /**
     * Get the "id" client with access control.
     *
     * @param id the id of the entity.
     * @param user the authenticated user.
     * @return the entity if accessible.
     */
    @Transactional(readOnly = true)
    public Optional<ClientDTO> findOneWithAccessControl(String id, User user) {
        if (user == null) {
            LOG.warn("Anonymous user access attempt for Client ID: {} - returning empty result", id);
            return Optional.empty();
        }

        LOG.debug("Request to get Client : {} with access control for user: {}", id, user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findOne(id);
        }

        if (user instanceof CupaUser cupaUser) {
            Set<String> merchantIds = cupaUser.getMerchantIdsSet();
        if (merchantIds.isEmpty()) {
                return Optional.empty();
            }
            return clientRepository.findByIdAndMerchantIds(id, merchantIds).map(clientMapper::toDto);
        } else return Optional.empty();
    }
}
