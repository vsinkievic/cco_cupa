package lt.creditco.cupa.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.ClientDetails;
import lt.creditco.cupa.remote.GatewayConfig;
import lt.creditco.cupa.remote.GatewayResponse;
import lt.creditco.cupa.remote.PaymentCurrency;
import lt.creditco.cupa.remote.PaymentReply;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public PaymentTransactionService(
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentTransactionMapper paymentTransactionMapper,
        PaymentMapper paymentMapper,
        ClientRepository clientRepository,
        MerchantRepository merchantRepository,
        UpGatewayClient upGatewayClient
    ) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.paymentMapper = paymentMapper;
        this.clientRepository = clientRepository;
        this.merchantRepository = merchantRepository;
        this.upGatewayClient = upGatewayClient;
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
        LOG.debug("Request to save PaymentTransaction : {}", paymentTransactionDTO);

        // Validate before saving
        validatePaymentTransaction(paymentTransactionDTO);

        PaymentTransaction paymentTransaction = paymentTransactionMapper.toEntity(paymentTransactionDTO);

        if (paymentTransaction.getId() == null) {
            paymentTransaction.setId(UUID.randomUUID().toString());
        }

        paymentTransaction.setStatus(TransactionStatus.RECEIVED);
        paymentTransaction = paymentTransactionRepository.save(paymentTransaction);

        placePayment(paymentTransaction, context);
        return paymentTransactionMapper.toDto(paymentTransaction);
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
        return paymentTransactionMapper.toDto(paymentTransaction);
    }

    private void placePayment(PaymentTransaction paymentTransaction, CupaApiContext.CupaApiContextData context) {
        GatewayConfig config = getGatewayConfig(context, paymentTransaction);

        lt.creditco.cupa.remote.PaymentRequest upPaymentRequest = upPaymentRequestFrom(paymentTransaction);
        GatewayResponse<PaymentReply> upResponse = upGatewayClient.placeTransaction(upPaymentRequest, config);

        if (upResponse.getResponse().getStatusCode() == 200) {
            paymentTransaction.setStatus(TransactionStatus.SUCCESS);
        } else {
            paymentTransaction.setStatus(TransactionStatus.FAILED);
        }

        paymentTransactionRepository.save(paymentTransaction);
    }

    private lt.creditco.cupa.remote.PaymentRequest upPaymentRequestFrom(PaymentTransaction paymentTransaction) {
        lt.creditco.cupa.remote.PaymentRequest upPaymentRequest = new lt.creditco.cupa.remote.PaymentRequest();
        upPaymentRequest.setOrderId(paymentTransaction.getOrderId());
        upPaymentRequest.setAmount(paymentTransaction.getAmount());
        upPaymentRequest.setCurrency(paymentTransaction.getCurrency().name());
        upPaymentRequest.setCardType(cardTypeFromPaymentBrand(paymentTransaction.getPaymentBrand()));

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
            .map(paymentTransactionMapper::toDto);
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
        return paymentTransactionRepository.findAll(pageable).map(paymentTransactionMapper::toDto);
    }

    /**
     * Get all the paymentTransactions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PaymentTransactionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentTransactionRepository.findAllWithEagerRelationships(pageable).map(paymentTransactionMapper::toDto);
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
        return paymentTransactionRepository.findOneWithEagerRelationships(id).map(paymentTransactionMapper::toDto);
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

        PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
        paymentTransactionDTO.setMerchantId(request.getMerchantId() != null ? request.getMerchantId() : context.getMerchantId());
        paymentTransactionDTO.setRequestTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        paymentTransactionDTO.setOrderId(request.getOrderId());
        paymentTransactionDTO.setClientId(request.getClientId());
        paymentTransactionDTO.setAmount(request.getAmount());
        paymentTransactionDTO.setCurrency(currencyFromPaymentCurrency(request.getCurrency()));
        paymentTransactionDTO.setPaymentBrand(paymentBrandFromCardType(request.getCardType()));

        paymentTransactionDTO.setStatus(TransactionStatus.RECEIVED);

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
}
