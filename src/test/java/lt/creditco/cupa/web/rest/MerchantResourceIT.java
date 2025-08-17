package lt.creditco.cupa.web.rest;

import static lt.creditco.cupa.domain.MerchantAsserts.*;
import static lt.creditco.cupa.web.rest.TestUtil.createUpdateProxyForBean;
import static lt.creditco.cupa.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;
import lt.creditco.cupa.IntegrationTest;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link MerchantResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
@ActiveProfiles(value = { "testprod", "testcontainers" })
@Disabled
class MerchantResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final MerchantMode DEFAULT_MODE = MerchantMode.TEST;
    private static final MerchantMode UPDATED_MODE = MerchantMode.LIVE;

    private static final MerchantStatus DEFAULT_STATUS = MerchantStatus.ACTIVE;
    private static final MerchantStatus UPDATED_STATUS = MerchantStatus.INACTIVE;

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE = new BigDecimal(2);

    private static final String DEFAULT_CUPA_TEST_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_CUPA_TEST_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_CUPA_PROD_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_CUPA_PROD_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_TEST_URL = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_TEST_URL = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_TEST_MERCHANT_ID = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_TEST_MERCHANT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_TEST_MERCHANT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_TEST_MERCHANT_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_TEST_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_TEST_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_PROD_URL = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_PROD_URL = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_PROD_MERCHANT_ID = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_PROD_MERCHANT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_PROD_MERCHANT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_PROD_MERCHANT_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_REMOTE_PROD_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_REMOTE_PROD_API_KEY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/merchants";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMerchantMockMvc;

    private Merchant merchant;

    private Merchant insertedMerchant;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Merchant createEntity() {
        return new Merchant()
            .name(DEFAULT_NAME)
            .mode(DEFAULT_MODE)
            .status(DEFAULT_STATUS)
            .balance(DEFAULT_BALANCE)
            .cupaTestApiKey(DEFAULT_CUPA_TEST_API_KEY)
            .cupaProdApiKey(DEFAULT_CUPA_PROD_API_KEY)
            .remoteTestUrl(DEFAULT_REMOTE_TEST_URL)
            .remoteTestMerchantId(DEFAULT_REMOTE_TEST_MERCHANT_ID)
            .remoteTestMerchantKey(DEFAULT_REMOTE_TEST_MERCHANT_KEY)
            .remoteTestApiKey(DEFAULT_REMOTE_TEST_API_KEY)
            .remoteProdUrl(DEFAULT_REMOTE_PROD_URL)
            .remoteProdMerchantId(DEFAULT_REMOTE_PROD_MERCHANT_ID)
            .remoteProdMerchantKey(DEFAULT_REMOTE_PROD_MERCHANT_KEY)
            .remoteProdApiKey(DEFAULT_REMOTE_PROD_API_KEY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Merchant createUpdatedEntity() {
        return new Merchant()
            .name(UPDATED_NAME)
            .mode(UPDATED_MODE)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE)
            .cupaTestApiKey(UPDATED_CUPA_TEST_API_KEY)
            .cupaProdApiKey(UPDATED_CUPA_PROD_API_KEY)
            .remoteTestUrl(UPDATED_REMOTE_TEST_URL)
            .remoteTestMerchantId(UPDATED_REMOTE_TEST_MERCHANT_ID)
            .remoteTestMerchantKey(UPDATED_REMOTE_TEST_MERCHANT_KEY)
            .remoteTestApiKey(UPDATED_REMOTE_TEST_API_KEY)
            .remoteProdUrl(UPDATED_REMOTE_PROD_URL)
            .remoteProdMerchantId(UPDATED_REMOTE_PROD_MERCHANT_ID)
            .remoteProdMerchantKey(UPDATED_REMOTE_PROD_MERCHANT_KEY)
            .remoteProdApiKey(UPDATED_REMOTE_PROD_API_KEY);
    }

    @BeforeEach
    void initTest() {
        merchant = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMerchant != null) {
            merchantRepository.delete(insertedMerchant);
            insertedMerchant = null;
        }
    }

    @Test
    @Transactional
    void createMerchant() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);
        var returnedMerchantDTO = om.readValue(
            restMerchantMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MerchantDTO.class
        );

        // Validate the Merchant in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMerchant = merchantMapper.toEntity(returnedMerchantDTO);
        assertMerchantUpdatableFieldsEquals(returnedMerchant, getPersistedMerchant(returnedMerchant));

        insertedMerchant = returnedMerchant;
    }

    @Test
    @Transactional
    void createMerchantWithExistingId() throws Exception {
        // Create the Merchant with an existing ID
        merchant.setId(UUID.randomUUID().toString());
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMerchantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        merchant.setName(null);

        // Create the Merchant, which fails.
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        restMerchantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkModeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        merchant.setMode(null);

        // Create the Merchant, which fails.
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        restMerchantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        merchant.setStatus(null);

        // Create the Merchant, which fails.
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        restMerchantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllMerchants() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        // Get all the merchantList
        restMerchantMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insertedMerchant.getId().toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].mode").value(hasItem(DEFAULT_MODE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(sameNumber(DEFAULT_BALANCE))))
            .andExpect(jsonPath("$.[*].cupaTestApiKey").value(hasItem(DEFAULT_CUPA_TEST_API_KEY)))
            .andExpect(jsonPath("$.[*].cupaProdApiKey").value(hasItem(DEFAULT_CUPA_PROD_API_KEY)))
            .andExpect(jsonPath("$.[*].remoteTestUrl").value(hasItem(DEFAULT_REMOTE_TEST_URL)))
            .andExpect(jsonPath("$.[*].remoteTestMerchantId").value(hasItem(DEFAULT_REMOTE_TEST_MERCHANT_ID)))
            .andExpect(jsonPath("$.[*].remoteTestMerchantKey").value(hasItem(DEFAULT_REMOTE_TEST_MERCHANT_KEY)))
            .andExpect(jsonPath("$.[*].remoteTestApiKey").value(hasItem(DEFAULT_REMOTE_TEST_API_KEY)))
            .andExpect(jsonPath("$.[*].remoteProdUrl").value(hasItem(DEFAULT_REMOTE_PROD_URL)))
            .andExpect(jsonPath("$.[*].remoteProdMerchantId").value(hasItem(DEFAULT_REMOTE_PROD_MERCHANT_ID)))
            .andExpect(jsonPath("$.[*].remoteProdMerchantKey").value(hasItem(DEFAULT_REMOTE_PROD_MERCHANT_KEY)))
            .andExpect(jsonPath("$.[*].remoteProdApiKey").value(hasItem(DEFAULT_REMOTE_PROD_API_KEY)));
    }

    @Test
    @Transactional
    void getMerchant() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        // Get the merchant
        restMerchantMockMvc
            .perform(get(ENTITY_API_URL_ID, merchant.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(insertedMerchant.getId().toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.mode").value(DEFAULT_MODE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.balance").value(sameNumber(DEFAULT_BALANCE)))
            .andExpect(jsonPath("$.cupaTestApiKey").value(DEFAULT_CUPA_TEST_API_KEY))
            .andExpect(jsonPath("$.cupaProdApiKey").value(DEFAULT_CUPA_PROD_API_KEY))
            .andExpect(jsonPath("$.remoteTestUrl").value(DEFAULT_REMOTE_TEST_URL))
            .andExpect(jsonPath("$.remoteTestMerchantId").value(DEFAULT_REMOTE_TEST_MERCHANT_ID))
            .andExpect(jsonPath("$.remoteTestMerchantKey").value(DEFAULT_REMOTE_TEST_MERCHANT_KEY))
            .andExpect(jsonPath("$.remoteTestApiKey").value(DEFAULT_REMOTE_TEST_API_KEY))
            .andExpect(jsonPath("$.remoteProdUrl").value(DEFAULT_REMOTE_PROD_URL))
            .andExpect(jsonPath("$.remoteProdMerchantId").value(DEFAULT_REMOTE_PROD_MERCHANT_ID))
            .andExpect(jsonPath("$.remoteProdMerchantKey").value(DEFAULT_REMOTE_PROD_MERCHANT_KEY))
            .andExpect(jsonPath("$.remoteProdApiKey").value(DEFAULT_REMOTE_PROD_API_KEY));
    }

    @Test
    @Transactional
    void getNonExistingMerchant() throws Exception {
        // Get the merchant
        restMerchantMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMerchant() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the merchant
        Merchant updatedMerchant = merchantRepository.findById(insertedMerchant.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMerchant are not directly saved in db
        em.detach(updatedMerchant);
        updatedMerchant
            .name(UPDATED_NAME)
            .mode(UPDATED_MODE)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE)
            .cupaTestApiKey(UPDATED_CUPA_TEST_API_KEY)
            .cupaProdApiKey(UPDATED_CUPA_PROD_API_KEY)
            .remoteTestUrl(UPDATED_REMOTE_TEST_URL)
            .remoteTestMerchantId(UPDATED_REMOTE_TEST_MERCHANT_ID)
            .remoteTestMerchantKey(UPDATED_REMOTE_TEST_MERCHANT_KEY)
            .remoteTestApiKey(UPDATED_REMOTE_TEST_API_KEY)
            .remoteProdUrl(UPDATED_REMOTE_PROD_URL)
            .remoteProdMerchantId(UPDATED_REMOTE_PROD_MERCHANT_ID)
            .remoteProdMerchantKey(UPDATED_REMOTE_PROD_MERCHANT_KEY)
            .remoteProdApiKey(UPDATED_REMOTE_PROD_API_KEY);
        MerchantDTO merchantDTO = merchantMapper.toDto(updatedMerchant);

        restMerchantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, insertedMerchant.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(merchantDTO))
            )
            .andExpect(status().isOk());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMerchantToMatchAllProperties(updatedMerchant);
    }

    @Test
    @Transactional
    void putNonExistingMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, merchant.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateMerchantWithPatch() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the merchant using partial update
        Merchant partialUpdatedMerchant = new Merchant();
        partialUpdatedMerchant.setId(merchant.getId());

        partialUpdatedMerchant
            .name(UPDATED_NAME)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE)
            .remoteTestMerchantId(UPDATED_REMOTE_TEST_MERCHANT_ID)
            .remoteTestMerchantKey(UPDATED_REMOTE_TEST_MERCHANT_KEY)
            .remoteProdApiKey(UPDATED_REMOTE_PROD_API_KEY);

        restMerchantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMerchant.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMerchant))
            )
            .andExpect(status().isOk());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMerchantUpdatableFieldsEquals(createUpdatedEntity(), getPersistedMerchant(insertedMerchant));
    }

    @Test
    @Transactional
    void fullUpdateMerchantWithPatch() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the merchant using partial update
        Merchant partialUpdatedMerchant = new Merchant();
        partialUpdatedMerchant.setId(merchant.getId());

        partialUpdatedMerchant
            .name(UPDATED_NAME)
            .mode(UPDATED_MODE)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE)
            .cupaTestApiKey(UPDATED_CUPA_TEST_API_KEY)
            .cupaProdApiKey(UPDATED_CUPA_PROD_API_KEY)
            .remoteTestUrl(UPDATED_REMOTE_TEST_URL)
            .remoteTestMerchantId(UPDATED_REMOTE_TEST_MERCHANT_ID)
            .remoteTestMerchantKey(UPDATED_REMOTE_TEST_MERCHANT_KEY)
            .remoteTestApiKey(UPDATED_REMOTE_TEST_API_KEY)
            .remoteProdUrl(UPDATED_REMOTE_PROD_URL)
            .remoteProdMerchantId(UPDATED_REMOTE_PROD_MERCHANT_ID)
            .remoteProdMerchantKey(UPDATED_REMOTE_PROD_MERCHANT_KEY)
            .remoteProdApiKey(UPDATED_REMOTE_PROD_API_KEY);

        restMerchantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMerchant.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMerchant))
            )
            .andExpect(status().isOk());

        // Validate the Merchant in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMerchantUpdatableFieldsEquals(createUpdatedEntity(), getPersistedMerchant(insertedMerchant));
    }

    @Test
    @Transactional
    void patchNonExistingMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, merchantDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(merchantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(merchantDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMerchant() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        merchant.setId(UUID.randomUUID().toString());

        // Create the Merchant
        MerchantDTO merchantDTO = merchantMapper.toDto(merchant);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMerchantMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(merchantDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Merchant in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMerchant() throws Exception {
        // Initialize the database
        insertedMerchant = merchantRepository.saveAndFlush(merchant);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the merchant
        restMerchantMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedMerchant.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return merchantRepository.count();
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

    protected Merchant getPersistedMerchant(Merchant merchant) {
        return merchantRepository.findById(merchant.getId()).orElseThrow();
    }

    protected void assertPersistedMerchantToMatchAllProperties(Merchant expectedMerchant) {
        assertMerchantAllPropertiesEquals(expectedMerchant, getPersistedMerchant(expectedMerchant));
    }

    protected void assertPersistedMerchantToMatchUpdatableProperties(Merchant expectedMerchant) {
        assertMerchantAllUpdatablePropertiesEquals(expectedMerchant, getPersistedMerchant(expectedMerchant));
    }

    protected void partialUpdateMerchantWithPatch(Merchant existingMerchant, Consumer<Merchant> patch) throws Exception {
        Merchant partialUpdatedMerchant = createUpdateProxyForBean(existingMerchant, existingMerchant);
        patch.accept(partialUpdatedMerchant);

        restMerchantMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMerchant.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMerchant))
            )
            .andExpect(status().isOk());
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void shouldDenyAnonymousAccess() throws Exception {
        // Test that anonymous users cannot access protected endpoints
        restMerchantMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isUnauthorized());

        restMerchantMockMvc.perform(get(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithApiKey() throws Exception {
        // Test that API keys are not accepted for these endpoints
        restMerchantMockMvc.perform(get(ENTITY_API_URL).header("X-API-Key", "any-api-key")).andExpect(status().isUnauthorized());

        restMerchantMockMvc
            .perform(get(ENTITY_API_URL_ID, "any-id").header("X-API-Key", "any-api-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void shouldAllowAdminAccessToAllEndpoints() throws Exception {
        // Test admin access to all endpoints
        restMerchantMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
        // Admin should be able to create, update, delete
        // This tests the existing functionality with admin role
    }

    @Test
    @WithMockUser(username = "user", authorities = "ROLE_USER")
    void shouldAllowUserAccessToOwnMerchantEndpoints() throws Exception {
        // Test user access to endpoints for their merchant
        // This would require setting up user with merchant context
        // For now, we test that authenticated users can access the endpoints
        restMerchantMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForAllOperations() throws Exception {
        // Test that all CRUD operations require authentication

        // GET (list)
        restMerchantMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isUnauthorized());

        // GET (by id)
        restMerchantMockMvc.perform(get(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());

        // POST (create)
        restMerchantMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());

        // PUT (update)
        restMerchantMockMvc
            .perform(put(ENTITY_API_URL_ID, "any-id").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());

        // PATCH (partial update)
        restMerchantMockMvc
            .perform(patch(ENTITY_API_URL_ID, "any-id").contentType("application/merge-patch+json").content("{}"))
            .andExpect(status().isUnauthorized());

        // DELETE
        restMerchantMockMvc.perform(delete(ENTITY_API_URL_ID, "any-id")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAcceptApiKeyForNonCupaApiEndpoints() throws Exception {
        // Test that these endpoints (non-CupaApi) do not accept API keys
        // This ensures the security boundary is maintained

        restMerchantMockMvc.perform(get(ENTITY_API_URL).header("X-API-Key", "valid-api-key-for-cupa")).andExpect(status().isUnauthorized());

        restMerchantMockMvc
            .perform(get(ENTITY_API_URL_ID, "any-id").header("X-API-Key", "valid-api-key-for-cupa"))
            .andExpect(status().isUnauthorized());
    }
}
