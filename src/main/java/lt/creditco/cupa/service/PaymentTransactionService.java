package lt.creditco.cupa.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.User;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.domain.util.Merger;
import lt.creditco.cupa.event.BalanceUpdateEvent;
import lt.creditco.cupa.event.MerchantBalanceUpdateEvent;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.ClientDetails;
import lt.creditco.cupa.remote.GatewayConfig;
import lt.creditco.cupa.remote.GatewayMessage;
import lt.creditco.cupa.remote.GatewayResponse;
import lt.creditco.cupa.remote.PaymentCurrency;
import lt.creditco.cupa.remote.PaymentReply;
import lt.creditco.cupa.remote.RestTemplateBodyInterceptor;
import lt.creditco.cupa.remote.UpGatewayClient;
import lt.creditco.cupa.repository.ClientRepository;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import lt.creditco.cupa.service.mapper.PaymentTransactionMapper;
import lt.creditco.cupa.web.context.CupaApiContext;
import lt.creditco.cupa.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.config.JHipsterProperties;

/**
 * Service Implementation for managing {@link lt.creditco.cupa.domain.PaymentTransaction}.
 */
@Service
@Transactional
public class PaymentTransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentTransactionService.class);

    private final PaymentTransactionRepository paymentTransactionRepository;

    private final PaymentTransactionMapper paymentTransactionMapper;

    private final PaymentMapper paymentMapper;

    private final ClientRepository clientRepository;

    private final MerchantRepository merchantRepository;

    private final UpGatewayClient upGatewayClient;

    private final RestTemplateBodyInterceptor bodyInterceptor;

    private final ApplicationEventPublisher eventPublisher;

    private final JHipsterProperties jHipsterProperties;

    private final Environment environment;

    public PaymentTransactionService(
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentTransactionMapper paymentTransactionMapper,
        PaymentMapper paymentMapper,
        ClientRepository clientRepository,
        MerchantRepository merchantRepository,
        UpGatewayClient upGatewayClient,
        RestTemplateBodyInterceptor bodyInterceptor,
        ApplicationEventPublisher eventPublisher,
        JHipsterProperties jHipsterProperties,
        Environment environment
    ) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.paymentMapper = paymentMapper;
        this.clientRepository = clientRepository;
        this.merchantRepository = merchantRepository;
        this.upGatewayClient = upGatewayClient;
        this.bodyInterceptor = bodyInterceptor;
        this.eventPublisher = eventPublisher;
        this.jHipsterProperties = jHipsterProperties;
        this.environment = environment;
    }

    /**
     * Check if the current profile is production.
     *
     * @return true if running in production profile, false otherwise
     */
    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate payment transaction data before saving.
     *
     * @param paymentTransactionDTO the entity to validate.
     * @throws BadRequestAlertException if validation fails.
     */
    private void validatePaymentTransaction(PaymentTransactionDTO paymentTransactionDTO) {
        // Validate client exists

        if (paymentTransactionDTO.getClientId() == null) {
            throw new BadRequestAlertException("Client ID is required", "PaymentTransaction", "clientIdRequired");
        }

        if (!clientRepository.existsById(paymentTransactionDTO.getClientId())) {
            Client client = clientRepository.findByMerchantClientId(paymentTransactionDTO.getClientId()).orElse(null);
            if (client == null) {
                throw new BadRequestAlertException(
                    "Client with ID=" + paymentTransactionDTO.getClientId() + " not found!",
                    "PaymentTransaction",
                    "clientNotFound"
                );
            }
            paymentTransactionDTO.setClientId(client.getId());
        }

        // Validate merchant exists
        if (paymentTransactionDTO.getMerchantId() == null) {
            throw new BadRequestAlertException("Merchant ID is required", "PaymentTransaction", "merchantIdRequired");
        }

        if (!merchantRepository.existsById(paymentTransactionDTO.getMerchantId())) {
            throw new BadRequestAlertException(
                "Merchant with ID=" + paymentTransactionDTO.getMerchantId() + " not found!",
                "PaymentTransaction",
                "merchantNotFound"
            );
        }

        // Validate amount is positive
        if (paymentTransactionDTO.getAmount() != null && paymentTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestAlertException("Amount must be greater than zero", "PaymentTransaction", "invalidAmount");
        }

        // Validate currency is not null
        if (paymentTransactionDTO.getCurrency() == null) {
            throw new BadRequestAlertException("Currency is required", "PaymentTransaction", "currencyRequired");
        }

        // Validate payment brand is not null
        if (paymentTransactionDTO.getPaymentBrand() == null) {
            throw new BadRequestAlertException("Payment brand is required", "PaymentTransaction", "paymentBrandRequired");
        }

        // Validate order ID is not null
        if (paymentTransactionDTO.getOrderId() == null || paymentTransactionDTO.getOrderId().trim().isEmpty()) {
            throw new BadRequestAlertException("Order ID is required", "PaymentTransaction", "orderIdRequired");
        }
    }

    /**
     * Save a paymentTransaction.
     *
     * @param paymentTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentTransactionDTO save(PaymentTransactionDTO paymentTransactionDTO, CupaApiContext.CupaApiContextData context) {
        LOG.debug("Request to save PaymentTransaction : {}, context: {}", paymentTransactionDTO, context);

        // Validate before saving
        validatePaymentTransaction(paymentTransactionDTO);

        // Set backoffice URL only for production profile
        if (isProdProfile()) {
            String baseUrl = jHipsterProperties.getMail().getBaseUrl();
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                String backofficeUrl = baseUrl.endsWith("/") ? baseUrl + "public/webhook" : baseUrl + "/public/webhook";
                paymentTransactionDTO.setBackofficeUrl(backofficeUrl);
            }
        }

        PaymentTransaction paymentTransaction = paymentTransactionMapper.toEntity(paymentTransactionDTO);

        if (paymentTransaction.getId() == null) {
            paymentTransaction.setId(UUID.randomUUID().toString());
        }

        // Check for duplicate orderId for the merchant
        if (
            paymentTransactionRepository.existsByMerchantIdAndOrderId(paymentTransaction.getMerchantId(), paymentTransaction.getOrderId())
        ) {
            throw new BadRequestAlertException("Duplicate OrderId", "PaymentTransaction", "duplicateOrderId");
        }

        paymentTransaction.setStatus(TransactionStatus.RECEIVED);
        paymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        placePayment(paymentTransaction, context);
        return enrichWithRelatedData(paymentTransactionMapper.toDto(paymentTransaction));
    }

    /**
     * Update a paymentTransaction.
     *
     * @param paymentTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentTransactionDTO update(PaymentTransactionDTO paymentTransactionDTO) {
        LOG.debug("Request to update PaymentTransaction : {}", paymentTransactionDTO);

        // Validate before updating
        validatePaymentTransaction(paymentTransactionDTO);

        PaymentTransaction paymentTransaction = paymentTransactionMapper.toEntity(paymentTransactionDTO);
        paymentTransaction = paymentTransactionRepository.save(paymentTransaction);
        return enrichWithRelatedData(paymentTransactionMapper.toDto(paymentTransaction));
    }

    public PaymentTransactionDTO queryPaymentFromGateway(String transactionId, CupaApiContext.CupaApiContextData context) {
        LOG.debug("Request to query PaymentTransaction : {}", transactionId);

        if (context.getMerchantContext() == null) {
            throw new BadRequestAlertException("Merchant context is required", "PaymentTransaction", "merchantContextRequired");
        }

        PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(transactionId).orElse(null);
        if (paymentTransaction == null) {
            throw new BadRequestAlertException("PaymentTransaction not found", "PaymentTransaction", "paymentTransactionNotFound");
        }

        if (!context.canAccessEntity(paymentTransaction)) {
            throw new BadRequestAlertException(
                String.format("You cannot query transactions for merchant: %s", paymentTransaction.getMerchantId()),
                "PaymentTransaction",
                "accessDenied"
            );
        }
        GatewayConfig config = getGatewayConfig(context, paymentTransaction);
        bodyInterceptor.clear();

        GatewayResponse<PaymentReply> upResponse = upGatewayClient.queryTransaction(paymentTransaction.getOrderId(), config);

        if (upResponse != null && upResponse.getResponse() != null) {
            if (upResponse.getResponse().getStatusCode() == 200) {
                RestTemplateBodyInterceptor.Trace trace = bodyInterceptor.getLastTrace();
                String responseBody = trace != null ? trace.getResponseBody() : null;
                paymentTransaction = paymentTransactionRepository // reread the payment transaction to get the latest state
                    .findById(paymentTransaction.getId())
                    .orElseThrow(() -> new RuntimeException("PaymentTransaction not found id=" + transactionId));
                paymentTransaction = mergeAndSaveIfNeeded(paymentTransaction, upResponse.getReply(), responseBody);
            }
        }

        // Publish event to update merchant balance
        if (paymentTransaction.getBalance() != null) {
            LOG.info(
                "Publishing merchant balance update event for merchantId: {}, balance: {}",
                paymentTransaction.getMerchantId(),
                paymentTransaction.getBalance()
            );
            eventPublisher.publishEvent(
                new MerchantBalanceUpdateEvent(this, paymentTransaction.getMerchantId(), paymentTransaction.getBalance())
            );
        }
        return enrichWithRelatedData(paymentTransactionMapper.toDto(paymentTransaction));
    }

    private void placePayment(PaymentTransaction paymentTransaction, CupaApiContext.CupaApiContextData context) {
        GatewayConfig config = getGatewayConfig(context, paymentTransaction);
        bodyInterceptor.clear();

        lt.creditco.cupa.remote.PaymentRequest upPaymentRequest = upPaymentRequestFrom(paymentTransaction);

        GatewayResponse<PaymentReply> upResponse = null;
        String statusDescription = null;
        try {
            upResponse = upGatewayClient.placeTransaction(upPaymentRequest, config);
        } catch (Exception e) {
            LOG.error("Error placing payment", e);
        }

        RestTemplateBodyInterceptor.Trace trace = bodyInterceptor.getLastTrace();
        if (trace != null) {
            paymentTransaction.setRequestData(trace.getRequestBody());
            paymentTransaction.setInitialResponseData(trace.getResponseBody());
        }
        if (upResponse != null) {
            if (upResponse.getResponse() == null) {
                paymentTransaction.setStatus(TransactionStatus.FAILED);
                statusDescription = "ERROR: Gateway response is null";
            } else if (
                upResponse.getResponse().getStatusCode() == 200 ||
                upResponse.getResponse().getStatusCode() == 201 ||
                upResponse.getResponse().getStatusCode() == 210
            ) {
                paymentTransaction.setStatus(TransactionStatus.PENDING);
                statusDescription = prepareStatusDescription(upResponse.getResponse());
                //                paymentTransaction.setTransactionId(upResponse.getResponse().ge
            } else {
                paymentTransaction.setStatus(TransactionStatus.FAILED);
                statusDescription = prepareStatusDescription(upResponse.getResponse());
            }
        }
        paymentTransaction.setStatusDescription(statusDescription);
        paymentTransactionRepository.save(paymentTransaction);
    }

    private lt.creditco.cupa.remote.PaymentRequest upPaymentRequestFrom(PaymentTransaction paymentTransaction) {
        lt.creditco.cupa.remote.PaymentRequest upPaymentRequest = new lt.creditco.cupa.remote.PaymentRequest();
        upPaymentRequest.setOrderId(paymentTransaction.getOrderId());
        upPaymentRequest.setAmount(paymentTransaction.getAmount());
        upPaymentRequest.setCurrency(paymentTransaction.getCurrency().name());
        upPaymentRequest.setCardType(cardTypeFromPaymentBrand(paymentTransaction.getPaymentBrand()));
        upPaymentRequest.setSendEmail(Objects.equals(paymentTransaction.getPaymentFlow(), PaymentFlow.EMAIL) ? 1 : 0);

        upPaymentRequest.setReplyUrl(paymentTransaction.getReplyUrl());
        upPaymentRequest.setBackofficeUrl(paymentTransaction.getBackofficeUrl());
        upPaymentRequest.setEcho(paymentTransaction.getEcho());

        Client client = clientRepository
            .findById(paymentTransaction.getClientId())
            .orElseThrow(() -> new BadRequestAlertException("Client not found", "PaymentTransaction", "clientNotFound"));
        upPaymentRequest.setClientId(client.getMerchantClientId());
        upPaymentRequest.setClient(upClientFrom(client));

        return upPaymentRequest;
    }

    private ClientDetails upClientFrom(Client client) {
        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setClientId(client.getMerchantClientId());
        clientDetails.setEmailAddress(client.getEmailAddress());
        clientDetails.setMobileNumber(client.getMobileNumber());
        clientDetails.setName(client.getName());
        clientDetails.setClientPhone(client.getClientPhone());

        return clientDetails;
    }

    private GatewayConfig getGatewayConfig(CupaApiContext.CupaApiContextData context, PaymentTransaction paymentTransaction) {
        if (context.getMerchantContext() == null) {
            throw new BadRequestAlertException("Merchant context is required", "PaymentTransaction", "merchantContextRequired");
        }

        return GatewayConfig.builder()
            .merchantMid(context.getMerchantContext().getGatewayMerchantId())
            .merchantKey(context.getMerchantContext().getGatewayMerchantKey())
            .apiKey(context.getMerchantContext().getGatewayApiKey())
            .baseUrl(context.getMerchantContext().getGatewayUrl())
            .replyUrl(context.getMerchantContext().getGatewayUrl())
            .backofficeUrl(context.getMerchantContext().getGatewayUrl())
            .merchantCurrency(paymentTransaction.getCurrency().name())
            .paymentType(paymentTransaction.getPaymentBrand().name())
            .build();
    }

    /**
     * Partially update a paymentTransaction.
     *
     * @param paymentTransactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PaymentTransactionDTO> partialUpdate(PaymentTransactionDTO paymentTransactionDTO) {
        LOG.debug("Request to partially update PaymentTransaction : {}", paymentTransactionDTO);

        return paymentTransactionRepository
            .findById(paymentTransactionDTO.getId())
            .map(existingPaymentTransaction -> {
                paymentTransactionMapper.partialUpdate(existingPaymentTransaction, paymentTransactionDTO);

                return existingPaymentTransaction;
            })
            .map(paymentTransactionRepository::save)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Get all the paymentTransactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all PaymentTransactions");
        return paymentTransactionRepository.findAll(pageable).map(paymentTransactionMapper::toDto).map(this::enrichWithRelatedData);
    }

    /**
     * Get all the paymentTransactions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PaymentTransactionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentTransactionRepository
            .findAllWithEagerRelationships(pageable)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Get one paymentTransaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDTO> findOne(String id) {
        LOG.debug("Request to get PaymentTransaction : {}", id);
        return paymentTransactionRepository
            .findOneWithEagerRelationships(id)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Get one paymentTransaction by merchantId and orderId.
     *
     * @param merchantId the merchant ID.
     * @param orderId the order ID.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDTO> findByMerchantIdAndOrderId(String merchantId, String orderId) {
        LOG.debug("Request to get PaymentTransaction by merchantId: {} and orderId: {}", merchantId, orderId);
        return paymentTransactionRepository
            .findByMerchantIdAndOrderId(merchantId, orderId)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Delete the paymentTransaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(String id) {
        LOG.debug("Request to delete PaymentTransaction : {}", id);
        paymentTransactionRepository.deleteById(id);
    }

    /**
     * Enrich PaymentTransactionDTO with related entity data.
     * This method loads the Client and Merchant entities to populate
     * merchantClientId, clientName, and merchantName fields.
     *
     * @param dto the DTO to enrich
     * @return the enriched DTO
     */
    private PaymentTransactionDTO enrichWithRelatedData(PaymentTransactionDTO dto) {
        if (dto == null) {
            return dto;
        }

        // Load and set client data
        if (dto.getClientId() != null) {
            clientRepository
                .findById(dto.getClientId())
                .ifPresent(client -> {
                    dto.setMerchantClientId(client.getMerchantClientId());
                    dto.setClientName(client.getName());
                });
        }

        // Load and set merchant data
        if (dto.getMerchantId() != null) {
            merchantRepository
                .findById(dto.getMerchantId())
                .ifPresent(merchant -> {
                    dto.setMerchantName(merchant.getName());
                });
        }

        return dto;
    }

    /**
     * Enrich a list of PaymentTransactionDTOs with related entity data.
     *
     * @param dtoList the list of DTOs to enrich
     * @return the enriched list
     */
    private List<PaymentTransactionDTO> enrichWithRelatedData(List<PaymentTransactionDTO> dtoList) {
        if (dtoList == null) {
            return dtoList;
        }

        return dtoList.stream().map(this::enrichWithRelatedData).toList();
    }

    /**
     * Enrich a page of PaymentTransactionDTOs with related entity data.
     *
     * @param dtoPage the page of DTOs to enrich
     * @return the enriched page
     */
    private Page<PaymentTransactionDTO> enrichWithRelatedData(Page<PaymentTransactionDTO> dtoPage) {
        if (dtoPage == null) {
            return dtoPage;
        }

        List<PaymentTransactionDTO> enrichedContent = enrichWithRelatedData(dtoPage.getContent());
        return dtoPage.map(dto -> enrichWithRelatedData(dto));
    }

    public Payment createPayment(PaymentRequest request, CupaApiContext.CupaApiContextData context) {
        LOG.info(
            "createPayment({}), executed by {}, merchant: {}, environment: {}",
            request.getOrderId(),
            context.getUser().getLogin(),
            context.getMerchantId(),
            context.getMerchantContext() != null && context.getMerchantContext().getMode() != null
                ? context.getMerchantContext().getMode().name()
                : null
        );

        LOG.debug("createPayment()....: request={}", request);
        LOG.debug("createPayment()....: context={}", context);
        LOG.debug(
            "createPayment()....: request.getMerchantId()={}, context.getMerchantId()={}",
            request.getMerchantId(),
            context.getMerchantId()
        );
        PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
        paymentTransactionDTO.setMerchantId(request.getMerchantId() != null ? request.getMerchantId() : context.getMerchantId());
        paymentTransactionDTO.setRequestTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        paymentTransactionDTO.setOrderId(request.getOrderId());
        paymentTransactionDTO.setClientId(request.getClientId());
        paymentTransactionDTO.setAmount(request.getAmount());
        paymentTransactionDTO.setCurrency(currencyFromPaymentCurrency(request.getCurrency()));
        paymentTransactionDTO.setPaymentBrand(paymentBrandFromCardType(request.getCardType()));
        paymentTransactionDTO.setPaymentFlow(request.getPaymentFlow());

        paymentTransactionDTO.setStatus(TransactionStatus.RECEIVED);

        if (paymentTransactionDTO.getMerchantId() == null) {
            throw new BadRequestAlertException("Merchant ID is required", "PaymentTransaction", "merchantIdRequired");
        }
        if (!context.canAccessEntity(paymentTransactionDTO)) {
            throw new BadRequestAlertException(
                String.format("You cannot post transactions for merchant: %s", request.getMerchantId()),
                "PaymentTransaction",
                "accessDenied"
            );
        }
        if (request.getClient() != null) {
            createOrUpdateClient(paymentTransactionDTO.getMerchantId(), paymentTransactionDTO.getClientId(), request.getClient());
        }

        paymentTransactionDTO = save(paymentTransactionDTO, context);

        Payment payment = paymentMapper.toPayment(paymentTransactionDTO);

        return payment;
    }

    private Currency currencyFromPaymentCurrency(PaymentCurrency paymentCurrency) {
        return Currency.valueOf(paymentCurrency.name());
    }

    private PaymentCurrency paymentCurrencyFromCurrency(Currency currency) {
        return PaymentCurrency.valueOf(currency.name());
    }

    private PaymentBrand paymentBrandFromCardType(CardType cardType) {
        return PaymentBrand.valueOf(cardType.name());
    }

    private CardType cardTypeFromPaymentBrand(PaymentBrand paymentBrand) {
        return CardType.valueOf(paymentBrand.name());
    }

    /**
     * Prepares a status description from the gateway response message.
     *
     * Priority order:
     * 1. If both detail and reason exist: detail + " " + reason
     * 2. If only detail exists: detail
     * 3. If only reason exists: reason
     * 4. If neither exists: message
     * 5. If message is null: "No status description available"
     *
     * @param response the gateway response message
     * @return the prepared status description
     */
    private String prepareStatusDescription(GatewayMessage response) {
        if (response == null) {
            return "No status description available";
        }

        String detail = response.getDetail();
        String reason = response.getReason();
        String message = response.getMessage();

        // Check if both detail and reason exist
        if (detail != null && !detail.trim().isEmpty() && reason != null && !reason.trim().isEmpty()) {
            return detail.trim() + ". " + reason.trim();
        }

        // Check if only detail exists
        if (detail != null && !detail.trim().isEmpty()) {
            return detail.trim();
        }

        // Check if only reason exists
        if (reason != null && !reason.trim().isEmpty()) {
            return reason.trim();
        }

        // Fall back to message
        if (message != null && !message.trim().isEmpty()) {
            return message.trim();
        }

        // Final fallback
        return "No status description available";
    }

    /**
     * Merges fields from PaymentReply into PaymentTransaction and saves if changes were made.
     * Uses the Merger utility to track changes and build a descriptive log.
     *
     * @param paymentTransaction the target PaymentTransaction entity
     * @param paymentReply the source PaymentReply from gateway response
     * @return the updated PaymentTransaction (saved if changes were made)
     */
    private PaymentTransaction mergeAndSaveIfNeeded(PaymentTransaction paymentTransaction, PaymentReply paymentReply, String responseBody) {
        if (paymentReply == null) {
            LOG.debug("PaymentReply is null, no merging needed for transaction: {}", paymentTransaction.getId());
            return paymentTransaction;
        }

        // Use the Merger utility to track changes
        Merger<PaymentTransaction> merger = Merger.of(paymentTransaction);

        // Merge relevant fields from PaymentReply to PaymentTransaction
        if (paymentReply.getAmount() != null) {
            merger.mergeBigDecimal("Amount", paymentTransaction::getAmount, paymentReply.getAmount(), paymentTransaction::setAmount);
        }
        TransactionStatus newStatus = getTransactionStatusFromReply(paymentReply);
        merger
            .mergeBigDecimal("Balance", paymentTransaction::getBalance, paymentReply.getBalance(), paymentTransaction::setBalance)
            .merge(
                "Status Description",
                paymentTransaction::getStatusDescription,
                paymentReply.getDetail(),
                paymentTransaction::setStatusDescription
            )
            .merge("Status", paymentTransaction::getStatus, newStatus, paymentTransaction::setStatus);

        // Check if any changes were made
        if (merger.hasChanges()) {
            if (paymentReply.getDate() != null) {
                paymentTransaction.setLastQueryData(responseBody);
            }
            // Log the changes with main payment fields for context
            String changeLog = merger.getChangeLog();
            LOG.info(
                "Payment transaction updated - ID: {}, MerchantID: {}, OrderID: {}, Changes: {}",
                paymentTransaction.getId(),
                paymentTransaction.getMerchantId(),
                paymentTransaction.getOrderId(),
                changeLog
            );

            // Save the updated transaction
            paymentTransaction = paymentTransactionRepository.save(paymentTransaction);
        } else {
            LOG.debug(
                "No changes detected for payment transaction - ID: {}, MerchantID: {}, OrderID: {}",
                paymentTransaction.getId(),
                paymentTransaction.getMerchantId(),
                paymentTransaction.getOrderId()
            );
        }

        return paymentTransaction;
    }

    /**
     * Determines the transaction status based on the PaymentReply result and success fields.
     *
     * @param paymentReply the payment reply from the gateway
     * @return the appropriate TransactionStatus
     */
    TransactionStatus getTransactionStatusFromReply(PaymentReply paymentReply) {
        if (paymentReply == null) {
            return null;
        }

        String result = paymentReply.getResult();
        String success = paymentReply.getSuccess();

        // Handle specific result values
        if ("0".equals(result)) {
            return TransactionStatus.SUCCESS;
        }
        if ("1".equals(result)) {
            return TransactionStatus.PENDING;
        }
        if ("11".equals(result)) {
            return TransactionStatus.ABANDONED;
        }

        // Handle other result values based on success field
        if ("Y".equals(success)) {
            return TransactionStatus.SUCCESS;
        }
        if ("N".equals(success)) {
            return TransactionStatus.FAILED;
        }

        // Default case - if we can't determine status, return null
        return null;
    }

    /**
     * Get all the payment transactions with access control based on user's merchant access.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    @Transactional(readOnly = true)
    public Page<PaymentTransactionDTO> findAllWithAccessControl(Pageable pageable, User user) {
        LOG.debug("Request to get all PaymentTransactions with access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findAll(pageable);
        }

        Set<String> merchantIds = user.getMerchantIdsSet();
        if (merchantIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return paymentTransactionRepository
            .findAllByMerchantIds(merchantIds, pageable)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Get all the payment transactions with eager load of many-to-many relationships and access control.
     *
     * @param pageable the pagination information.
     * @param user the authenticated user.
     * @return the list of entities filtered by user's merchant access.
     */
    public Page<PaymentTransactionDTO> findAllWithEagerRelationshipsWithAccessControl(Pageable pageable, User user) {
        LOG.debug("Request to get all PaymentTransactions with eager relationships and access control for user: {}", user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findAllWithEagerRelationships(pageable);
        }

        Set<String> merchantIds = user.getMerchantIdsSet();
        if (merchantIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return paymentTransactionRepository
            .findAllByMerchantIds(merchantIds, pageable)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Get the "id" payment transaction with access control.
     *
     * @param id the id of the entity.
     * @param user the authenticated user.
     * @return the entity if accessible.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentTransactionDTO> findOneWithAccessControl(String id, User user) {
        LOG.debug("Request to get PaymentTransaction : {} with access control for user: {}", id, user.getLogin());

        if (user.hasAuthority("ROLE_ADMIN")) {
            return findOne(id);
        }

        Set<String> merchantIds = user.getMerchantIdsSet();
        if (merchantIds.isEmpty()) {
            return Optional.empty();
        }

        return paymentTransactionRepository
            .findByIdAndMerchantIds(id, merchantIds)
            .map(paymentTransactionMapper::toDto)
            .map(this::enrichWithRelatedData);
    }

    /**
     * Create or update a client. Only updates the database if at least one value differs.
     *
     * @param merchantId the merchant ID
     * @param clientId the client ID
     * @param paymentClient the client data from payment request
     */
    private void createOrUpdateClient(String merchantId, String clientId, lt.creditco.cupa.api.PaymentClient paymentClient) {
        if (paymentClient == null) {
            return;
        }

        // Try to find existing client by merchantClientId
        Client existingClient = clientRepository.findByMerchantClientId(clientId).orElse(null);

        if (existingClient != null) {
            // Update existing client only if values differ
            boolean needsUpdate = false;

            if (paymentClient.getName() != null && !paymentClient.getName().equals(existingClient.getName())) {
                existingClient.setName(paymentClient.getName());
                needsUpdate = true;
            }

            if (paymentClient.getEmailAddress() != null && !paymentClient.getEmailAddress().equals(existingClient.getEmailAddress())) {
                existingClient.setEmailAddress(paymentClient.getEmailAddress());
                needsUpdate = true;
            }

            if (paymentClient.getMobileNumber() != null && !paymentClient.getMobileNumber().equals(existingClient.getMobileNumber())) {
                existingClient.setMobileNumber(paymentClient.getMobileNumber());
                needsUpdate = true;
            }

            // Only save if something changed
            if (needsUpdate) {
                existingClient.setUpdatedInGateway(Instant.now());
                clientRepository.save(existingClient);
                LOG.debug("Updated existing client: {}", clientId);
            }
        } else {
            // Create new client
            Client newClient = new Client();
            newClient.setId(UUID.randomUUID().toString());
            newClient.setMerchantClientId(clientId);
            newClient.setMerchantId(merchantId);
            newClient.setName(paymentClient.getName());
            newClient.setEmailAddress(paymentClient.getEmailAddress());
            newClient.setMobileNumber(paymentClient.getMobileNumber());
            newClient.setValid(true);
            newClient.setCreatedInGateway(Instant.now());
            newClient.setUpdatedInGateway(Instant.now());

            clientRepository.save(newClient);
            LOG.debug("Created new client: {}", clientId);
        }
    }

    /**
     * Process webhook notification from payment gateway.
     * This method handles incoming webhook notifications and updates the corresponding payment transaction.
     * Verifies the signature before processing the webhook.
     *
     * @param paymentReply the payment reply from the gateway
     * @return true if webhook was processed successfully, false if signature verification failed or processing failed
     */
    public boolean processWebhook(PaymentReply paymentReply) {
        LOG.info(
            "Processing webhook for OrderID: {}, MerchantID: {}, Success: {}",
            paymentReply.getOrderId(),
            paymentReply.getMerchantId(),
            paymentReply.getSuccess()
        );

        if (paymentReply.getOrderId() == null || paymentReply.getMerchantId() == null) {
            LOG.warn(
                "Webhook missing required fields - OrderID: {}, MerchantID: {}",
                paymentReply.getOrderId(),
                paymentReply.getMerchantId()
            );
            return false;
        }

        Merchant merchant = merchantRepository
            .findByRemoteTestMerchantId(paymentReply.getMerchantId())
            .orElse(merchantRepository.findByRemoteProdMerchantId(paymentReply.getMerchantId()).orElse(null));
        if (merchant == null) {
            LOG.warn("Merchant not found for remote ID: {}", paymentReply.getMerchantId());
            return false;
        }

        // Get merchant key for signature verification
        String merchantKey = merchant.getMerchantKeyByMode();
        if (merchantKey == null) {
            LOG.error("Cannot verify signature: merchant key not found for MerchantID: {}", paymentReply.getMerchantId());
            return false;
        }

        // Verify signature
        if (!lt.creditco.cupa.remote.SignatureVerifier.verifyWebhookSignature(paymentReply, merchantKey)) {
            LOG.error(
                "Signature verification failed for webhook - MerchantID: {}, OrderID: {}",
                paymentReply.getMerchantId(),
                paymentReply.getOrderId()
            );
            return false;
        }

        // Find the payment transaction by merchant ID and order ID
        PaymentTransaction paymentTransaction = paymentTransactionRepository
            .findByMerchantIdAndOrderId(merchant.getId(), paymentReply.getOrderId())
            .orElse(null);
        if (paymentTransaction == null) {
            LOG.warn(
                "No payment transaction found for webhook - resolvedMerchantID: {}, OrderID: {}, remoteMerchantID: {}",
                merchant.getId(),
                paymentReply.getOrderId(),
                paymentReply.getMerchantId()
            );
            return false;
        }

        // Check if the merchant ID in the payment transaction matches the resolved merchant ID
        if (!merchant.getId().equals(paymentTransaction.getMerchantId())) {
            LOG.warn(
                "Merchant ID mismatch for transaction id = {}: resolvedMerchantID: {}, paymentTransactionMerchantID: {}",
                paymentTransaction.getId(),
                merchant.getId(),
                paymentTransaction.getMerchantId()
            );
        }

        // Update the transaction with webhook data
        paymentTransaction = mergeAndSaveIfNeeded(paymentTransaction, paymentReply, "Webhook notification");

        // Check if balance is null and fire event for asynchronous balance update
        if (paymentTransaction.getBalance() == null) {
            LOG.info(
                "Balance is null after webhook processing, firing balance update event for transaction: {}",
                paymentTransaction.getId()
            );
            eventPublisher.publishEvent(
                new BalanceUpdateEvent(
                    this,
                    paymentTransaction.getId(),
                    paymentTransaction.getMerchantId(),
                    paymentTransaction.getOrderId()
                )
            );
        }

        return true;
    }
}
