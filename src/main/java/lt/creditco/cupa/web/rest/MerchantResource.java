package lt.creditco.cupa.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.UserRepository;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link lt.creditco.cupa.domain.Merchant}.
 * All authenticated users can access merchant data with role-based field visibility.
 */
@RestController
@RequestMapping("/api/merchants")
public class MerchantResource {

    private static final Logger LOG = LoggerFactory.getLogger(MerchantResource.class);

    private static final String ENTITY_NAME = "merchant";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MerchantService merchantService;

    private final MerchantRepository merchantRepository;

    private final UserRepository userRepository;

    public MerchantResource(MerchantService merchantService, MerchantRepository merchantRepository, UserRepository userRepository) {
        this.merchantService = merchantService;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get the current authenticated user from the principal.
     *
     * @param principal the authenticated principal
     * @return the current user
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            LOG.warn("Anonymous user access attempt - returning empty results");
            return null;
        }

        // Try to find user by login (principal.getName()) with authorities eagerly loaded
        User user = userRepository.findOneWithAuthoritiesByLogin(principal.getName()).orElse(null);
        if (user != null) {
            return user;
        }

        LOG.warn("User not found for principal: {} - returning empty results", principal.getName());
        return null;
    }

    /**
     * {@code POST  /merchants} : Create a new merchant.
     *
     * @param merchantDTO the merchantDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new merchantDTO, or with status {@code 400 (Bad Request)} if the merchant has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<MerchantDTO> createMerchant(@Valid @RequestBody MerchantDTO merchantDTO) throws URISyntaxException {
        LOG.debug("REST request to save Merchant : {}", merchantDTO);
        if (StringUtils.isBlank(merchantDTO.getId())) {
            throw new BadRequestAlertException("ID is required", ENTITY_NAME, "idrequired");
        }
        if (merchantDTO.getVersion() != null) {
            throw new BadRequestAlertException("A new merchant cannot already have an ID", ENTITY_NAME, "idexists");
        }
        merchantDTO = merchantService.save(merchantDTO);
        return ResponseEntity.created(new URI("/api/merchants/" + merchantDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, merchantDTO.getId().toString()))
            .body(merchantDTO);
    }

    /**
     * {@code PUT  /merchants/:id} : Updates an existing merchant.
     *
     * @param id the id of the merchantDTO to save.
     * @param merchantDTO the merchantDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated merchantDTO,
     * or with status {@code 400 (Bad Request)} if the merchantDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the merchantDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<MerchantDTO> updateMerchant(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody MerchantDTO merchantDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Merchant : {}, {}", id, merchantDTO);
        if (merchantDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, merchantDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!merchantRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        merchantDTO = merchantService.update(merchantDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, merchantDTO.getId().toString()))
            .body(merchantDTO);
    }

    /**
     * {@code PATCH  /merchants/:id} : Partial updates given fields of an existing merchant, field will ignore if it is null
     *
     * @param id the id of the merchantDTO to save.
     * @param merchantDTO the merchantDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated merchantDTO,
     * or with status {@code 400 (Bad Request)} if the merchantDTO is not valid,
     * or with status {@code 404 (Not Found)} if the merchantDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the merchantDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<MerchantDTO> partialUpdateMerchant(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody MerchantDTO merchantDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Merchant partially : {}, {}", id, merchantDTO);
        if (merchantDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, merchantDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!merchantRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MerchantDTO> result = merchantService.partialUpdate(merchantDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, merchantDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /merchants} : get all the merchants.
     * Accessible to all authenticated users with role-based field visibility.
     *
     * @param pageable the pagination information.
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of merchants in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MerchantDTO>> getAllMerchants(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        Principal principal
    ) {
        //        LOG.debug("REST request to get a page of Merchants");
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return empty page for anonymous users
            Page<MerchantDTO> emptyPage = Page.empty(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), emptyPage);
            return ResponseEntity.ok().headers(headers).body(emptyPage.getContent());
        }

        Page<MerchantDTO> page = merchantService.findAllWithAccessControl(pageable, currentUser);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /merchants/:id} : get the "id" merchant.
     * Accessible to all authenticated users with role-based field visibility.
     *
     * @param id the id of the merchantDTO to retrieve.
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the merchantDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MerchantDTO> getMerchant(@PathVariable("id") String id, Principal principal) {
        //        LOG.debug("REST request to get Merchant : {}", id);
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return 404 for anonymous users
            return ResponseEntity.notFound().build();
        }

        Optional<MerchantDTO> merchantDTO = merchantService.findOneWithAccessControl(id, currentUser);
        return ResponseUtil.wrapOrNotFound(merchantDTO);
    }

    /**
     * {@code DELETE  /merchants/:id} : delete the "id" merchant.
     *
     * @param id the id of the merchantDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteMerchant(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Merchant : {}", id);
        merchantService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
