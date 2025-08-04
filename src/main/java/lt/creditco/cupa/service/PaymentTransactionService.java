package lt.creditco.cupa.service;

import java.util.Optional;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentTransactionMapper;
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

    public PaymentTransactionService(
        PaymentTransactionRepository paymentTransactionRepository,
        PaymentTransactionMapper paymentTransactionMapper
    ) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentTransactionMapper = paymentTransactionMapper;
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
    public Optional<PaymentTransactionDTO> findOne(Long id) {
        LOG.debug("Request to get PaymentTransaction : {}", id);
        return paymentTransactionRepository.findOneWithEagerRelationships(id).map(paymentTransactionMapper::toDto);
    }

    /**
     * Delete the paymentTransaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete PaymentTransaction : {}", id);
        paymentTransactionRepository.deleteById(id);
    }
}
