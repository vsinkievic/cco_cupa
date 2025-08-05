package lt.creditco.cupa.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.repository.ClientCardRepository;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
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
 * REST controller for managing {@link lt.creditco.cupa.domain.ClientCard}.
 */
@RestController
@RequestMapping("/api/client-cards")
public class ClientCardResource {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCardResource.class);

    private static final String ENTITY_NAME = "clientCard";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ClientCardService clientCardService;

    private final ClientCardRepository clientCardRepository;

    public ClientCardResource(ClientCardService clientCardService, ClientCardRepository clientCardRepository) {
        this.clientCardService = clientCardService;
        this.clientCardRepository = clientCardRepository;
    }

    /**
     * {@code POST  /client-cards} : Create a new clientCard.
     *
     * @param clientCardDTO the clientCardDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new clientCardDTO, or with status {@code 400 (Bad Request)} if the clientCard has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ClientCardDTO> createClientCard(@Valid @RequestBody ClientCardDTO clientCardDTO) throws URISyntaxException {
        LOG.debug("REST request to save ClientCard : {}", clientCardDTO);
        if (clientCardDTO.getId() != null) {
            throw new BadRequestAlertException("A new clientCard cannot already have an ID", ENTITY_NAME, "idexists");
        }
        clientCardDTO = clientCardService.save(clientCardDTO);
        return ResponseEntity.created(new URI("/api/client-cards/" + clientCardDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, clientCardDTO.getId().toString()))
            .body(clientCardDTO);
    }

    /**
     * {@code PUT  /client-cards/:id} : Updates an existing clientCard.
     *
     * @param id the id of the clientCardDTO to save.
     * @param clientCardDTO the clientCardDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clientCardDTO,
     * or with status {@code 400 (Bad Request)} if the clientCardDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the clientCardDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientCardDTO> updateClientCard(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ClientCardDTO clientCardDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ClientCard : {}, {}", id, clientCardDTO);
        if (clientCardDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clientCardDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clientCardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        clientCardDTO = clientCardService.update(clientCardDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clientCardDTO.getId().toString()))
            .body(clientCardDTO);
    }

    /**
     * {@code PATCH  /client-cards/:id} : Partial updates given fields of an existing clientCard, field will ignore if it is null
     *
     * @param id the id of the clientCardDTO to save.
     * @param clientCardDTO the clientCardDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated clientCardDTO,
     * or with status {@code 400 (Bad Request)} if the clientCardDTO is not valid,
     * or with status {@code 404 (Not Found)} if the clientCardDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the clientCardDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ClientCardDTO> partialUpdateClientCard(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ClientCardDTO clientCardDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ClientCard partially : {}, {}", id, clientCardDTO);
        if (clientCardDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, clientCardDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!clientCardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ClientCardDTO> result = clientCardService.partialUpdate(clientCardDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, clientCardDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /client-cards} : get all the clientCards.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of clientCards in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ClientCardDTO>> getAllClientCards(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of ClientCards");
        Page<ClientCardDTO> page;
        if (eagerload) {
            page = clientCardService.findAllWithEagerRelationships(pageable);
        } else {
            page = clientCardService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /client-cards/:id} : get the "id" clientCard.
     *
     * @param id the id of the clientCardDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the clientCardDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientCardDTO> getClientCard(@PathVariable("id") String id) {
        LOG.debug("REST request to get ClientCard : {}", id);
        Optional<ClientCardDTO> clientCardDTO = clientCardService.findOne(id);
        return ResponseUtil.wrapOrNotFound(clientCardDTO);
    }

    /**
     * {@code DELETE  /client-cards/:id} : delete the "id" clientCard.
     *
     * @param id the id of the clientCardDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientCard(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ClientCard : {}", id);
        clientCardService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
