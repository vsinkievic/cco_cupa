package lt.creditco.cupa.service;

import java.util.Optional;
import java.util.UUID;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.PaymentCurrency;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import lt.creditco.cupa.service.mapper.PaymentTransactionMapper;
import lt.creditco.cupa.web.context.CupaApiContext;
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

    public PaymentTransactionService(
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentTransactionMapper paymentTransactionMapper,
        PaymentMapper paymentMapper
    ) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Save a paymentTransaction.
     *
     * @param paymentTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentTransactionDTO save(PaymentTransactionDTO paymentTransactionDTO) {
        LOG.debug("Request to save PaymentTransaction : {}", paymentTransactionDTO);
        PaymentTransaction paymentTransaction = paymentTransactionMapper.toEntity(paymentTransactionDTO);

        if (paymentTransaction.getId() == null) {
            paymentTransaction.setId(UUID.randomUUID().toString());
        }

        paymentTransaction.setStatus(TransactionStatus.RECEIVED);
        paymentTransaction = paymentTransactionRepository.save(paymentTransaction);
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
        PaymentTransaction paymentTransaction = paymentTransactionMapper.toEntity(paymentTransactionDTO);
        paymentTransaction = paymentTransactionRepository.save(paymentTransaction);
        return paymentTransactionMapper.toDto(paymentTransaction);
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
            context.getEnvironment()
        );

        PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
        paymentTransactionDTO.setMerchantId(request.getMerchantId() != null ? request.getMerchantId() : context.getMerchantId());
        paymentTransactionDTO.setOrderId(request.getOrderId());
        paymentTransactionDTO.setClientId(request.getClientId());
        paymentTransactionDTO.setAmount(request.getAmount());
        paymentTransactionDTO.setCurrency(currencyFromPaymentCurrency(request.getCurrency()));
        paymentTransactionDTO.setPaymentBrand(paymentBrandFromCardType(request.getCardType()));

        paymentTransactionDTO.setStatus(TransactionStatus.RECEIVED);

        paymentTransactionDTO = save(paymentTransactionDTO);

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
}
