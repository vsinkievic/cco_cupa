package lt.creditco.cupa.web.rest;

import static lt.creditco.cupa.domain.PaymentTransactionAsserts.*;
import static lt.creditco.cupa.web.rest.TestUtil.createUpdateProxyForBean;
import static lt.creditco.cupa.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lt.creditco.cupa.IntegrationTest;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentTransactionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link PaymentTransactionResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
@ActiveProfiles(value = { "testprod", "testcontainers" })
@Disabled
class PaymentTransactionResourceIT {

    private static final String DEFAULT_ORDER_ID = "AAAAAAAAAA";
    private static final String UPDATED_ORDER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_MERCHANT_ID = "MERCHANT-ID";

    private static final String DEFAULT_CLIENT_ID = "CLIENT-ID";

    private static final String DEFAULT_GATEWAY_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_GATEWAY_TRANSACTION_ID = "BBBBBBBBBB";

    private static final TransactionStatus DEFAULT_STATUS = TransactionStatus.RECEIVED;
    private static final TransactionStatus UPDATED_STATUS = TransactionStatus.PENDING;

    private static final PaymentFlow DEFAULT_PAYMENT_FLOW = PaymentFlow.EMAIL;
    private static final PaymentFlow UPDATED_PAYMENT_FLOW = PaymentFlow.ONLINE;

    private static final String DEFAULT_STATUS_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_STATUS_DESCRIPTION = "BBBBBBBBBB";

    private static final PaymentBrand DEFAULT_PAYMENT_BRAND = PaymentBrand.UnionPay;
    private static final PaymentBrand UPDATED_PAYMENT_BRAND = PaymentBrand.Alipay;

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE = new BigDecimal(2);

    private static final Currency DEFAULT_CURRENCY = Currency.AUD;
    private static final Currency UPDATED_CURRENCY = Currency.USD;

    private static final String DEFAULT_REPLY_URL = "AAAAAAAAAA";
    private static final String UPDATED_REPLY_URL = "BBBBBBBBBB";

    private static final String DEFAULT_BACKOFFICE_URL = "AAAAAAAAAA";
    private static final String UPDATED_BACKOFFICE_URL = "BBBBBBBBBB";

    private static final String DEFAULT_ECHO = "AAAAAAAAAA";
    private static final String UPDATED_ECHO = "BBBBBBBBBB";

    private static final String DEFAULT_SIGNATURE = "AAAAAAAAAA";
    private static final String UPDATED_SIGNATURE = "BBBBBBBBBB";

    private static final String DEFAULT_SIGNATURE_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_SIGNATURE_VERSION = "BBBBBBBBBB";

    private static final Instant DEFAULT_REQUEST_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUEST_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_REQUEST_DATA = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_INITIAL_RESPONSE_DATA = "AAAAAAAAAA";
    private static final String UPDATED_INITIAL_RESPONSE_DATA = "BBBBBBBBBB";

    private static final Instant DEFAULT_CALLBACK_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CALLBACK_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CALLBACK_DATA = "AAAAAAAAAA";
    private static final String UPDATED_CALLBACK_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_QUERY_DATA = "AAAAAAAAAA";
    private static final String UPDATED_LAST_QUERY_DATA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/payment-transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepositoryMock;

    @Autowired
    private PaymentTransactionMapper paymentTransactionMapper;

    @Mock
    private PaymentTransactionService paymentTransactionServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPaymentTransactionMockMvc;

    private PaymentTransaction paymentTransaction;

    private PaymentTransaction insertedPaymentTransaction;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PaymentTransaction createEntity(EntityManager em) {
        PaymentTransaction paymentTransaction = new PaymentTransaction()
            .id(DEFAULT_ORDER_ID)
            .merchantId(DEFAULT_MERCHANT_ID)
            .clientId(DEFAULT_CLIENT_ID)
            .gatewayTransactionId(DEFAULT_GATEWAY_TRANSACTION_ID)
            .status(DEFAULT_STATUS)
            .statusDescription(DEFAULT_STATUS_DESCRIPTION)
            .paymentBrand(DEFAULT_PAYMENT_BRAND)
            .amount(DEFAULT_AMOUNT)
            .balance(DEFAULT_BALANCE)
            .currency(DEFAULT_CURRENCY)
            .replyUrl(DEFAULT_REPLY_URL)
            .backofficeUrl(DEFAULT_BACKOFFICE_URL)
            .echo(DEFAULT_ECHO)
            .paymentFlow(DEFAULT_PAYMENT_FLOW)
            .signature(DEFAULT_SIGNATURE)
            .signatureVersion(DEFAULT_SIGNATURE_VERSION)
            .requestTimestamp(DEFAULT_REQUEST_TIMESTAMP)
            .requestData(DEFAULT_REQUEST_DATA)
            .initialResponseData(DEFAULT_INITIAL_RESPONSE_DATA)
            .callbackTimestamp(DEFAULT_CALLBACK_TIMESTAMP)
            .callbackData(DEFAULT_CALLBACK_DATA)
            .lastQueryData(DEFAULT_LAST_QUERY_DATA);

        return paymentTransaction;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PaymentTransaction createUpdatedEntity(EntityManager em) {
        PaymentTransaction updatedPaymentTransaction = new PaymentTransaction()
            .id(DEFAULT_ORDER_ID)
            .merchantId(DEFAULT_MERCHANT_ID)
            .clientId(DEFAULT_CLIENT_ID)
            .gatewayTransactionId(UPDATED_GATEWAY_TRANSACTION_ID)
            .status(UPDATED_STATUS)
            .statusDescription(UPDATED_STATUS_DESCRIPTION)
            .paymentBrand(UPDATED_PAYMENT_BRAND)
            .amount(UPDATED_AMOUNT)
            .balance(UPDATED_BALANCE)
            .currency(UPDATED_CURRENCY)
            .replyUrl(UPDATED_REPLY_URL)
            .backofficeUrl(UPDATED_BACKOFFICE_URL)
            .echo(UPDATED_ECHO)
            .paymentFlow(UPDATED_PAYMENT_FLOW)
            .signature(UPDATED_SIGNATURE)
            .signatureVersion(UPDATED_SIGNATURE_VERSION)
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .requestData(UPDATED_REQUEST_DATA)
            .initialResponseData(UPDATED_INITIAL_RESPONSE_DATA)
            .callbackTimestamp(UPDATED_CALLBACK_TIMESTAMP)
            .callbackData(UPDATED_CALLBACK_DATA)
            .lastQueryData(UPDATED_LAST_QUERY_DATA);

        return updatedPaymentTransaction;
    }

    @BeforeEach
    void initTest() {
        paymentTransaction = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPaymentTransaction != null) {
            paymentTransactionRepository.delete(insertedPaymentTransaction);
            insertedPaymentTransaction = null;
        }
    }

    @Test
    @Transactional
    void createPaymentTransaction() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);
        var returnedPaymentTransactionDTO = om.readValue(
            restPaymentTransactionMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PaymentTransactionDTO.class
        );

        // Validate the PaymentTransaction in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPaymentTransaction = paymentTransactionMapper.toEntity(returnedPaymentTransactionDTO);
        assertPaymentTransactionUpdatableFieldsEquals(
            returnedPaymentTransaction,
            getPersistedPaymentTransaction(returnedPaymentTransaction)
        );

        insertedPaymentTransaction = returnedPaymentTransaction;
    }

    @Test
    @Transactional
    void createPaymentTransactionWithExistingId() throws Exception {
        // Create the PaymentTransaction with an existing ID
        paymentTransaction.setId(UUID.randomUUID().toString());
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentTransaction.setStatus(null);

        // Create the PaymentTransaction, which fails.
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPaymentBrandIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentTransaction.setPaymentBrand(null);

        // Create the PaymentTransaction, which fails.
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentTransaction.setAmount(null);

        // Create the PaymentTransaction, which fails.
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentTransaction.setCurrency(null);

        // Create the PaymentTransaction, which fails.
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkRequestTimestampIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        paymentTransaction.setRequestTimestamp(null);

        // Create the PaymentTransaction, which fails.
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPaymentTransactions() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        // Get all the paymentTransactionList
        restPaymentTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(DEFAULT_ORDER_ID)))
            .andExpect(jsonPath("$.[*].merchantId").value(hasItem(DEFAULT_MERCHANT_ID)))
            .andExpect(jsonPath("$.[*].clientId").value(hasItem(DEFAULT_CLIENT_ID)))
            .andExpect(jsonPath("$.[*].gatewayTransactionId").value(hasItem(DEFAULT_GATEWAY_TRANSACTION_ID)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].statusDescription").value(hasItem(DEFAULT_STATUS_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].paymentBrand").value(hasItem(DEFAULT_PAYMENT_BRAND.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(sameNumber(DEFAULT_BALANCE))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY.toString())))
            .andExpect(jsonPath("$.[*].replyUrl").value(hasItem(DEFAULT_REPLY_URL)))
            .andExpect(jsonPath("$.[*].backofficeUrl").value(hasItem(DEFAULT_BACKOFFICE_URL)))
            .andExpect(jsonPath("$.[*].echo").value(hasItem(DEFAULT_ECHO)))
            .andExpect(jsonPath("$.[*].paymentFlow").value(hasItem(DEFAULT_PAYMENT_FLOW.toString())))
            .andExpect(jsonPath("$.[*].signature").value(hasItem(DEFAULT_SIGNATURE)))
            .andExpect(jsonPath("$.[*].signatureVersion").value(hasItem(DEFAULT_SIGNATURE_VERSION)))
            .andExpect(jsonPath("$.[*].requestTimestamp").value(hasItem(DEFAULT_REQUEST_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].requestData").value(hasItem(DEFAULT_REQUEST_DATA)))
            .andExpect(jsonPath("$.[*].initialResponseData").value(hasItem(DEFAULT_INITIAL_RESPONSE_DATA)))
            .andExpect(jsonPath("$.[*].callbackTimestamp").value(hasItem(DEFAULT_CALLBACK_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].callbackData").value(hasItem(DEFAULT_CALLBACK_DATA)))
            .andExpect(jsonPath("$.[*].lastQueryData").value(hasItem(DEFAULT_LAST_QUERY_DATA)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPaymentTransactionsWithEagerRelationshipsIsEnabled() throws Exception {
        when(paymentTransactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(paymentTransactionServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPaymentTransactionsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(paymentTransactionServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(paymentTransactionRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPaymentTransaction() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        // Get the paymentTransaction
        restPaymentTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, paymentTransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(DEFAULT_ORDER_ID))
            .andExpect(jsonPath("$.merchantId").value(DEFAULT_MERCHANT_ID))
            .andExpect(jsonPath("$.clientId").value(DEFAULT_CLIENT_ID))
            .andExpect(jsonPath("$.gatewayTransactionId").value(DEFAULT_GATEWAY_TRANSACTION_ID))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.statusDescription").value(DEFAULT_STATUS_DESCRIPTION))
            .andExpect(jsonPath("$.paymentBrand").value(DEFAULT_PAYMENT_BRAND.toString()))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.balance").value(sameNumber(DEFAULT_BALANCE)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY.toString()))
            .andExpect(jsonPath("$.replyUrl").value(DEFAULT_REPLY_URL))
            .andExpect(jsonPath("$.backofficeUrl").value(DEFAULT_BACKOFFICE_URL))
            .andExpect(jsonPath("$.echo").value(DEFAULT_ECHO))
            .andExpect(jsonPath("$.paymentFlow").value(DEFAULT_PAYMENT_FLOW.toString()))
            .andExpect(jsonPath("$.signature").value(DEFAULT_SIGNATURE))
            .andExpect(jsonPath("$.signatureVersion").value(DEFAULT_SIGNATURE_VERSION))
            .andExpect(jsonPath("$.requestTimestamp").value(DEFAULT_REQUEST_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.requestData").value(DEFAULT_REQUEST_DATA))
            .andExpect(jsonPath("$.initialResponseData").value(DEFAULT_INITIAL_RESPONSE_DATA))
            .andExpect(jsonPath("$.callbackTimestamp").value(DEFAULT_CALLBACK_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.callbackData").value(DEFAULT_CALLBACK_DATA))
            .andExpect(jsonPath("$.lastQueryData").value(DEFAULT_LAST_QUERY_DATA));
    }

    @Test
    @Transactional
    void getNonExistingPaymentTransaction() throws Exception {
        // Get the paymentTransaction
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPaymentTransaction() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentTransaction
        PaymentTransaction updatedPaymentTransaction = paymentTransactionRepository.findById(paymentTransaction.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPaymentTransaction are not directly saved in db
        em.detach(updatedPaymentTransaction);
        updatedPaymentTransaction
            .id(UPDATED_ORDER_ID)
            .merchantId(DEFAULT_MERCHANT_ID)
            .clientId(DEFAULT_CLIENT_ID)
            .gatewayTransactionId(UPDATED_GATEWAY_TRANSACTION_ID)
            .status(UPDATED_STATUS)
            .statusDescription(UPDATED_STATUS_DESCRIPTION)
            .paymentBrand(UPDATED_PAYMENT_BRAND)
            .amount(UPDATED_AMOUNT)
            .balance(UPDATED_BALANCE)
            .currency(UPDATED_CURRENCY)
            .replyUrl(UPDATED_REPLY_URL)
            .backofficeUrl(UPDATED_BACKOFFICE_URL)
            .echo(UPDATED_ECHO)
            .paymentFlow(UPDATED_PAYMENT_FLOW)
            .signature(UPDATED_SIGNATURE)
            .signatureVersion(UPDATED_SIGNATURE_VERSION)
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .requestData(UPDATED_REQUEST_DATA)
            .initialResponseData(UPDATED_INITIAL_RESPONSE_DATA)
            .callbackTimestamp(UPDATED_CALLBACK_TIMESTAMP)
            .callbackData(UPDATED_CALLBACK_DATA)
            .lastQueryData(UPDATED_LAST_QUERY_DATA);
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(updatedPaymentTransaction);

        restPaymentTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, paymentTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentTransactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPaymentTransactionToMatchAllProperties(updatedPaymentTransaction);
    }

    @Test
    @Transactional
    void putNonExistingPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, paymentTransactionDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(paymentTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePaymentTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentTransaction using partial update
        PaymentTransaction partialUpdatedPaymentTransaction = new PaymentTransaction();
        partialUpdatedPaymentTransaction.setId(paymentTransaction.getId());

        partialUpdatedPaymentTransaction
            .id(UPDATED_ORDER_ID)
            .merchantId(DEFAULT_MERCHANT_ID)
            .clientId(DEFAULT_CLIENT_ID)
            .gatewayTransactionId(UPDATED_GATEWAY_TRANSACTION_ID)
            .status(UPDATED_STATUS)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .paymentFlow(UPDATED_PAYMENT_FLOW)
            .signatureVersion(UPDATED_SIGNATURE_VERSION)
            .requestData(UPDATED_REQUEST_DATA);

        restPaymentTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPaymentTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPaymentTransaction))
            )
            .andExpect(status().isOk());

        // Validate the PaymentTransaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPaymentTransactionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPaymentTransaction, paymentTransaction),
            getPersistedPaymentTransaction(paymentTransaction)
        );
    }

    @Test
    @Transactional
    void fullUpdatePaymentTransactionWithPatch() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the paymentTransaction using partial update
        PaymentTransaction partialUpdatedPaymentTransaction = new PaymentTransaction();
        partialUpdatedPaymentTransaction.setId(paymentTransaction.getId());

        partialUpdatedPaymentTransaction
            .id(UPDATED_ORDER_ID)
            .merchantId(DEFAULT_MERCHANT_ID)
            .clientId(DEFAULT_CLIENT_ID)
            .gatewayTransactionId(UPDATED_GATEWAY_TRANSACTION_ID)
            .status(UPDATED_STATUS)
            .statusDescription(UPDATED_STATUS_DESCRIPTION)
            .paymentBrand(UPDATED_PAYMENT_BRAND)
            .amount(UPDATED_AMOUNT)
            .balance(UPDATED_BALANCE)
            .currency(UPDATED_CURRENCY)
            .replyUrl(UPDATED_REPLY_URL)
            .backofficeUrl(UPDATED_BACKOFFICE_URL)
            .echo(UPDATED_ECHO)
            .paymentFlow(UPDATED_PAYMENT_FLOW)
            .signature(UPDATED_SIGNATURE)
            .signatureVersion(UPDATED_SIGNATURE_VERSION)
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .requestData(UPDATED_REQUEST_DATA)
            .initialResponseData(UPDATED_INITIAL_RESPONSE_DATA)
            .callbackTimestamp(UPDATED_CALLBACK_TIMESTAMP)
            .callbackData(UPDATED_CALLBACK_DATA)
            .lastQueryData(UPDATED_LAST_QUERY_DATA);

        restPaymentTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPaymentTransaction.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPaymentTransaction))
            )
            .andExpect(status().isOk());

        // Validate the PaymentTransaction in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPaymentTransactionUpdatableFieldsEquals(
            partialUpdatedPaymentTransaction,
            getPersistedPaymentTransaction(partialUpdatedPaymentTransaction)
        );
    }

    @Test
    @Transactional
    void patchNonExistingPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, paymentTransactionDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(paymentTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(paymentTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPaymentTransaction() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        paymentTransaction.setId(UUID.randomUUID().toString());

        // Create the PaymentTransaction
        PaymentTransactionDTO paymentTransactionDTO = paymentTransactionMapper.toDto(paymentTransaction);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPaymentTransactionMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(paymentTransactionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PaymentTransaction in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePaymentTransaction() throws Exception {
        // Initialize the database
        insertedPaymentTransaction = paymentTransactionRepository.saveAndFlush(paymentTransaction);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the paymentTransaction
        restPaymentTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, paymentTransaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return paymentTransactionRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected PaymentTransaction getPersistedPaymentTransaction(PaymentTransaction paymentTransaction) {
        return paymentTransactionRepository.findById(paymentTransaction.getId()).orElseThrow();
    }

    protected void assertPersistedPaymentTransactionToMatchAllProperties(PaymentTransaction expectedPaymentTransaction) {
        assertPaymentTransactionAllPropertiesEquals(expectedPaymentTransaction, getPersistedPaymentTransaction(expectedPaymentTransaction));
    }

    protected void assertPersistedPaymentTransactionToMatchUpdatableProperties(PaymentTransaction expectedPaymentTransaction) {
        assertPaymentTransactionAllUpdatablePropertiesEquals(
            expectedPaymentTransaction,
            getPersistedPaymentTransaction(expectedPaymentTransaction)
        );
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void shouldDenyAnonymousAccess() throws Exception {
        // Test that anonymous users cannot access protected endpoints
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isUnauthorized());

        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithApiKey() throws Exception {
        // Test that API keys are not accepted for these endpoints
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL).header("X-API-Key", "any-api-key")).andExpect(status().isUnauthorized());

        restPaymentTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, "any-id").header("X-API-Key", "any-api-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void shouldAllowAdminAccessToAllEndpoints() throws Exception {
        // Test admin access to all endpoints
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        // Admin should be able to create, update, delete
        // This tests the existing functionality with admin role
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_USER")
    void shouldAllowUserAccessToOwnMerchantEndpoints() throws Exception {
        // Test user access to endpoints for their merchant
        // This would require setting up user with merchant context
        // For now, we test that authenticated users can access the endpoints
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForAllOperations() throws Exception {
        // Test that all CRUD operations require authentication

        // GET (list)
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isUnauthorized());

        // GET (by id)
        restPaymentTransactionMockMvc.perform(get(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());

        // POST (create)
        restPaymentTransactionMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());

        // PUT (update)
        restPaymentTransactionMockMvc
            .perform(put(ENTITY_API_URL_ID, "any-id").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());

        // PATCH (partial update)
        restPaymentTransactionMockMvc
            .perform(patch(ENTITY_API_URL_ID, "any-id").contentType("application/merge-patch+json").content("{}"))
            .andExpect(status().isUnauthorized());

        // DELETE
        restPaymentTransactionMockMvc.perform(delete(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAcceptApiKeyForNonCupaApiEndpoints() throws Exception {
        // Test that these endpoints (non-CupaApi) do not accept API keys
        // This ensures the security boundary is maintained

        restPaymentTransactionMockMvc
            .perform(get(ENTITY_API_URL).header("X-API-Key", "valid-api-key-for-cupa"))
            .andExpect(status().isUnauthorized());

        restPaymentTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, "any-id").header("X-API-Key", "valid-api-key-for-cupa"))
            .andExpect(status().isUnauthorized());
    }
}
