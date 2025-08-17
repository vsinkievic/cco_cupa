package lt.creditco.cupa.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.repository.UserRepository;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.web.context.CupaApiContext;
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
 * REST controller for managing {@link lt.creditco.cupa.domain.PaymentTransaction}.
 */
@RestController
@RequestMapping("/api/payment-transactions")
public class PaymentTransactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentTransactionResource.class);

    private static final String ENTITY_NAME = "paymentTransaction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaymentTransactionService paymentTransactionService;

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final UserRepository userRepository;

    private final CupaApiBusinessLogicService businessLogicService;

    public PaymentTransactionResource(
        PaymentTransactionService paymentTransactionService,
        PaymentTransactionRepository paymentTransactionRepository,
        UserRepository userRepository,
        CupaApiBusinessLogicService businessLogicService
    ) {
        this.paymentTransactionService = paymentTransactionService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.userRepository = userRepository;
        this.businessLogicService = businessLogicService;
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
     * {@code POST  /payment-transactions} : Create a new paymentTransaction.
     *
     * @param paymentTransactionDTO the paymentTransactionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new paymentTransactionDTO, or with status {@code 400 (Bad Request)} if the paymentTransaction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PaymentTransactionDTO> createPaymentTransaction(
        @Valid @RequestBody PaymentTransactionDTO paymentTransactionDTO,
        HttpServletRequest request,
        Principal principal
    ) throws URISyntaxException {
        LOG.debug("REST request to save PaymentTransaction : {}", paymentTransactionDTO);
        if (paymentTransactionDTO.getId() != null) {
            throw new BadRequestAlertException("A new paymentTransaction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (paymentTransactionDTO.getRequestTimestamp() == null) {
            paymentTransactionDTO.setRequestTimestamp(Instant.now());
        }
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentTransactionDTO, principal);
        paymentTransactionDTO = paymentTransactionService.save(paymentTransactionDTO, context);
        return ResponseEntity.created(new URI("/api/payment-transactions/" + paymentTransactionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, paymentTransactionDTO.getId().toString()))
            .body(paymentTransactionDTO);
    }

    /**
     * {@code PUT  /payment-transactions/:id} : Updates an existing paymentTransaction.
     *
     * @param id the id of the paymentTransactionDTO to save.
     * @param paymentTransactionDTO the paymentTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated paymentTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the paymentTransactionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the paymentTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PaymentTransactionDTO> updatePaymentTransaction(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody PaymentTransactionDTO paymentTransactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update PaymentTransaction : {}, {}", id, paymentTransactionDTO);
        if (paymentTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, paymentTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!paymentTransactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        paymentTransactionDTO = paymentTransactionService.update(paymentTransactionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, paymentTransactionDTO.getId().toString()))
            .body(paymentTransactionDTO);
    }

    /**
     * {@code PATCH  /payment-transactions/:id} : Partial updates given fields of an existing paymentTransaction, field will ignore if it is null
     *
     * @param id the id of the paymentTransactionDTO to save.
     * @param paymentTransactionDTO the paymentTransactionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated paymentTransactionDTO,
     * or with status {@code 400 (Bad Request)} if the paymentTransactionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the paymentTransactionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the paymentTransactionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PaymentTransactionDTO> partialUpdatePaymentTransaction(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody PaymentTransactionDTO paymentTransactionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update PaymentTransaction partially : {}, {}", id, paymentTransactionDTO);
        if (paymentTransactionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, paymentTransactionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!paymentTransactionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PaymentTransactionDTO> result = paymentTransactionService.partialUpdate(paymentTransactionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, paymentTransactionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /payment-transactions} : get all the paymentTransactions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of paymentTransactions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<PaymentTransactionDTO>> getAllPaymentTransactions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload,
        Principal principal
    ) {
        LOG.debug("REST request to get a page of PaymentTransactions");
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return empty page for anonymous users
            Page<PaymentTransactionDTO> emptyPage = Page.empty(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), emptyPage);
            return ResponseEntity.ok().headers(headers).body(emptyPage.getContent());
        }

        Page<PaymentTransactionDTO> page;
        if (eagerload) {
            page = paymentTransactionService.findAllWithEagerRelationshipsWithAccessControl(pageable, currentUser);
        } else {
            page = paymentTransactionService.findAllWithAccessControl(pageable, currentUser);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /payment-transactions/:id} : get the "id" paymentTransaction.
     *
     * @param id the id of the paymentTransactionDTO to retrieve.
     * @param principal the authenticated principal
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the paymentTransactionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionDTO> getPaymentTransaction(@PathVariable("id") String id, Principal principal) {
        LOG.debug("REST request to get PaymentTransaction : {}", id);
        User currentUser = getCurrentUser(principal);

        if (currentUser == null) {
            // Return 404 for anonymous users
            return ResponseEntity.notFound().build();
        }

        Optional<PaymentTransactionDTO> paymentTransactionDTO = paymentTransactionService.findOneWithAccessControl(id, currentUser);
        return ResponseUtil.wrapOrNotFound(paymentTransactionDTO);
    }

    /**
     * {@code DELETE  /payment-transactions/:id} : delete the "id" paymentTransaction.
     *
     * @param id the id of the paymentTransactionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentTransaction(@PathVariable("id") String id) {
        LOG.debug("REST request to delete PaymentTransaction : {}", id);
        paymentTransactionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
