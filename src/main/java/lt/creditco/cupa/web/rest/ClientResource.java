package lt.creditco.cupa.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.repository.ClientRepository;
import lt.creditco.cupa.repository.UserRepository;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link lt.creditco.cupa.domain.Client}.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientResource {

    private static final Logger LOG = LoggerFactory.getLogger(ClientResource.class);

    private static final String ENTITY_NAME = "client";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ClientService clientService;

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    public ClientResource(ClientService clientService, ClientRepository clientRepository, UserRepository userRepository) {
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get the current authenticated user from the principal.
     *
     * @param principal the authenticated principal
     * @return the current user, or null if anonymous
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return null;
        }

        // Try to find user by login (principal.getName()) with authorities eagerly loaded
        Optional<User> userOpt = userRepository.findOneWithAuthoritiesByLogin(principal.getName());
        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        LOG.warn("User not found for principal: {} - returning empty results", principal.getName());
        return null;
    }

    /**
     * {@code POST  /clients} : Create a new client.
     *
     * @param clientDTO the clientDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new clientDTO, or with status {@code 400 (Bad Request)} if the client has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ClientDTO> createClient(@Valid @RequestBody ClientDTO clientDTO) throws URISyntaxException {
        LOG.debug("REST request to save Client : {}", clientDTO);
        if (clientDTO.getId() != null) {
            if (clientRepository.existsById(clientDTO.getId())) {
                throw new BadRequestAlertException("Such ID already exists", ENTITY_NAME, "idexists");
            }
        } else {
            clientDTO.setId(UUID.randomUUID().toString());
        }
        clientDTO = clientService.save(clientDTO);
        return ResponseEntity.created(new URI("/api/clients/" + clientDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, clientDTO.getId().toString()))
            .body(clientDTO);
    }

    /**
     * {@code PUT  /clients/:id} : Updates an existing client.
     *
     * @param id the id of the clientDTO to save.
     * @param clientDTO the clientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clientDTO,
     * or with status {@code 400 (Bad Request)} if the clientDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the clientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> updateClient(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ClientDTO clientDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Client : {}, {}", id, clientDTO);
        if (clientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        clientDTO = clientService.update(clientDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clientDTO.getId().toString()))
            .body(clientDTO);
    }

    /**
     * {@code PATCH  /clients/:id} : Partial updates given fields of an existing client, field will ignore if it is null
     *
     * @param id the id of the clientDTO to save.
     * @param clientDTO the clientDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clientDTO,
     * or with status {@code 400 (Bad Request)} if the clientDTO is not valid,
     * or with status {@code 404 (Not Found)} if the clientDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the clientDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ClientDTO> partialUpdateClient(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ClientDTO clientDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Client partially : {}, {}", id, clientDTO);
        if (clientDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clientDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clientRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ClientDTO> result = clientService.partialUpdate(clientDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clientDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /clients} : get all the clients.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of clients in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ClientDTO>> getAllClients(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        Principal principal
    ) {
        LOG.debug("REST request to get a page of Clients");
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return empty page for anonymous users
            Page<ClientDTO> emptyPage = Page.empty(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), emptyPage);
            return ResponseEntity.ok().headers(headers).body(emptyPage.getContent());
        }

        Page<ClientDTO> page;
        if (eagerload) {
            page = clientService.findAllWithEagerRelationshipsWithAccessControl(pageable, currentUser);
        } else {
            page = clientService.findAllWithAccessControl(pageable, currentUser);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /clients/:id} : get the "id" client.
     *
     * @param id the id of the clientDTO to retrieve.
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the clientDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable("id") String id, Principal principal) {
        LOG.debug("REST request to get Client : {}", id);
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return 404 for anonymous users
            return ResponseEntity.notFound().build();
        }

        Optional<ClientDTO> clientDTO = clientService.findOneWithAccessControl(id, currentUser);
        return ResponseUtil.wrapOrNotFound(clientDTO);
    }

    /**
     * {@code DELETE  /clients/:id} : delete the "id" client.
     *
     * @param id the id of the clientDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Client : {}", id);
        clientService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
