package lt.creditco.cupa.web.rest;

import static lt.creditco.cupa.domain.ClientCardAsserts.*;
import static lt.creditco.cupa.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lt.creditco.cupa.IntegrationTest;
import lt.creditco.cupa.domain.Client;
import lt.creditco.cupa.domain.ClientCard;
import lt.creditco.cupa.repository.ClientCardRepository;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.mapper.ClientCardMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ClientCardResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ClientCardResourceIT {

    private static final String DEFAULT_MASKED_PAN = "AAAAAAAAAA";
    private static final String UPDATED_MASKED_PAN = "BBBBBBBBBB";

    private static final String DEFAULT_EXPIRY_DATE = "AAAAAAAAAA";
    private static final String UPDATED_EXPIRY_DATE = "BBBBBBBBBB";

    private static final String DEFAULT_CARDHOLDER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_CARDHOLDER_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_IS_DEFAULT = false;
    private static final Boolean UPDATED_IS_DEFAULT = true;

    private static final Boolean DEFAULT_IS_VALID = false;
    private static final Boolean UPDATED_IS_VALID = true;

    private static final String ENTITY_API_URL = "/api/client-cards";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ClientCardRepository clientCardRepository;

    @Mock
    private ClientCardRepository clientCardRepositoryMock;

    @Autowired
    private ClientCardMapper clientCardMapper;

    @Mock
    private ClientCardService clientCardServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restClientCardMockMvc;

    private ClientCard clientCard;

    private ClientCard insertedClientCard;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClientCard createEntity(EntityManager em) {
        ClientCard clientCard = new ClientCard()
            .maskedPan(DEFAULT_MASKED_PAN)
            .expiryDate(DEFAULT_EXPIRY_DATE)
            .cardholderName(DEFAULT_CARDHOLDER_NAME)
            .isDefault(DEFAULT_IS_DEFAULT)
            .isValid(DEFAULT_IS_VALID);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createEntity(em);
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        clientCard.setClient(client);
        return clientCard;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClientCard createUpdatedEntity(EntityManager em) {
        ClientCard updatedClientCard = new ClientCard()
            .maskedPan(UPDATED_MASKED_PAN)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .cardholderName(UPDATED_CARDHOLDER_NAME)
            .isDefault(UPDATED_IS_DEFAULT)
            .isValid(UPDATED_IS_VALID);
        // Add required entity
        Client client;
        if (TestUtil.findAll(em, Client.class).isEmpty()) {
            client = ClientResourceIT.createUpdatedEntity(em);
            em.persist(client);
            em.flush();
        } else {
            client = TestUtil.findAll(em, Client.class).get(0);
        }
        updatedClientCard.setClient(client);
        return updatedClientCard;
    }

    @BeforeEach
    public void initTest() {
        clientCard = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedClientCard != null) {
            clientCardRepository.delete(insertedClientCard);
            insertedClientCard = null;
        }
    }

    @Test
    @Transactional
    void createClientCard() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);
        var returnedClientCardDTO = om.readValue(
            restClientCardMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(clientCardDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ClientCardDTO.class
        );

        // Validate the ClientCard in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedClientCard = clientCardMapper.toEntity(returnedClientCardDTO);
        assertClientCardUpdatableFieldsEquals(returnedClientCard, getPersistedClientCard(returnedClientCard));

        insertedClientCard = returnedClientCard;
    }

    @Test
    @Transactional
    void createClientCardWithExistingId() throws Exception {
        // Create the ClientCard with an existing ID
        clientCard.setId(UUID.randomUUID());
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restClientCardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(clientCardDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkMaskedPanIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        clientCard.setMaskedPan(null);

        // Create the ClientCard, which fails.
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        restClientCardMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(clientCardDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllClientCards() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        // Get all the clientCardList
        restClientCardMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insertedClientCard.getId().toString())))
            .andExpect(jsonPath("$.[*].maskedPan").value(hasItem(DEFAULT_MASKED_PAN)))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE)))
            .andExpect(jsonPath("$.[*].cardholderName").value(hasItem(DEFAULT_CARDHOLDER_NAME)))
            .andExpect(jsonPath("$.[*].isDefault").value(hasItem(DEFAULT_IS_DEFAULT)))
            .andExpect(jsonPath("$.[*].isValid").value(hasItem(DEFAULT_IS_VALID)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClientCardsWithEagerRelationshipsIsEnabled() throws Exception {
        when(clientCardServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClientCardMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(clientCardServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllClientCardsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(clientCardServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restClientCardMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(clientCardRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getClientCard() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        // Get the clientCard
        restClientCardMockMvc
            .perform(get(ENTITY_API_URL_ID, insertedClientCard.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(insertedClientCard.getId().toString()))
            .andExpect(jsonPath("$.maskedPan").value(DEFAULT_MASKED_PAN))
            .andExpect(jsonPath("$.expiryDate").value(DEFAULT_EXPIRY_DATE))
            .andExpect(jsonPath("$.cardholderName").value(DEFAULT_CARDHOLDER_NAME))
            .andExpect(jsonPath("$.isDefault").value(DEFAULT_IS_DEFAULT))
            .andExpect(jsonPath("$.isValid").value(DEFAULT_IS_VALID));
    }

    @Test
    @Transactional
    void getNonExistingClientCard() throws Exception {
        // Get the clientCard
        restClientCardMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingClientCard() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the clientCard
        ClientCard updatedClientCard = clientCardRepository.findById(clientCard.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedClientCard are not directly saved in db
        em.detach(updatedClientCard);
        updatedClientCard
            .maskedPan(UPDATED_MASKED_PAN)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .cardholderName(UPDATED_CARDHOLDER_NAME)
            .isDefault(UPDATED_IS_DEFAULT)
            .isValid(UPDATED_IS_VALID);
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(updatedClientCard);

        restClientCardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, insertedClientCard.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(clientCardDTO))
            )
            .andExpect(status().isOk());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedClientCardToMatchAllProperties(updatedClientCard);
    }

    @Test
    @Transactional
    void putNonExistingClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, clientCardDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(clientCardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(clientCardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(clientCardDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateClientCardWithPatch() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the clientCard using partial update
        ClientCard partialUpdatedClientCard = new ClientCard();
        partialUpdatedClientCard.setId(clientCard.getId());

        partialUpdatedClientCard
            .expiryDate(UPDATED_EXPIRY_DATE)
            .cardholderName(UPDATED_CARDHOLDER_NAME)
            .isDefault(UPDATED_IS_DEFAULT)
            .isValid(UPDATED_IS_VALID);

        restClientCardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClientCard.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClientCard))
            )
            .andExpect(status().isOk());

        // Validate the ClientCard in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClientCardUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedClientCard, clientCard),
            getPersistedClientCard(clientCard)
        );
    }

    @Test
    @Transactional
    void fullUpdateClientCardWithPatch() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the clientCard using partial update
        ClientCard partialUpdatedClientCard = new ClientCard();
        partialUpdatedClientCard.setId(clientCard.getId());

        partialUpdatedClientCard
            .maskedPan(UPDATED_MASKED_PAN)
            .expiryDate(UPDATED_EXPIRY_DATE)
            .cardholderName(UPDATED_CARDHOLDER_NAME)
            .isDefault(UPDATED_IS_DEFAULT)
            .isValid(UPDATED_IS_VALID);

        restClientCardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClientCard.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClientCard))
            )
            .andExpect(status().isOk());

        // Validate the ClientCard in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertClientCardUpdatableFieldsEquals(partialUpdatedClientCard, getPersistedClientCard(partialUpdatedClientCard));
    }

    @Test
    @Transactional
    void patchNonExistingClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, clientCardDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(clientCardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(clientCardDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamClientCard() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        clientCard.setId(UUID.randomUUID());

        // Create the ClientCard
        ClientCardDTO clientCardDTO = clientCardMapper.toDto(clientCard);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restClientCardMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(clientCardDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ClientCard in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteClientCard() throws Exception {
        // Initialize the database
        insertedClientCard = clientCardRepository.saveAndFlush(clientCard);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the clientCard
        restClientCardMockMvc
            .perform(delete(ENTITY_API_URL_ID, clientCard.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return clientCardRepository.count();
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

    protected ClientCard getPersistedClientCard(ClientCard clientCard) {
        return clientCardRepository.findById(clientCard.getId()).orElseThrow();
    }

    protected void assertPersistedClientCardToMatchAllProperties(ClientCard expectedClientCard) {
        assertClientCardAllPropertiesEquals(expectedClientCard, getPersistedClientCard(expectedClientCard));
    }

    protected void assertPersistedClientCardToMatchUpdatableProperties(ClientCard expectedClientCard) {
        assertClientCardAllUpdatablePropertiesEquals(expectedClientCard, getPersistedClientCard(expectedClientCard));
    }

    protected void assertClientCardUpdatableFieldsEquals(ClientCard expected, ClientCard actual) {
        assertThat(expected)
            .satisfies(e -> assertThat(e.getMaskedPan()).as("check maskedPan").isEqualTo(actual.getMaskedPan()))
            .satisfies(e -> assertThat(e.getExpiryDate()).as("check expiryDate").isEqualTo(actual.getExpiryDate()))
            .satisfies(e -> assertThat(e.getCardholderName()).as("check cardholderName").isEqualTo(actual.getCardholderName()))
            .satisfies(e -> assertThat(e.getIsDefault()).as("check isDefault").isEqualTo(actual.getIsDefault()))
            .satisfies(e -> assertThat(e.getIsValid()).as("check isValid").isEqualTo(actual.getIsValid()));
    }

    protected void assertClientCardAllPropertiesEquals(ClientCard expected, ClientCard actual) {
        assertThat(expected)
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()))
            .satisfies(e -> assertThat(e.getMaskedPan()).as("check maskedPan").isEqualTo(actual.getMaskedPan()))
            .satisfies(e -> assertThat(e.getExpiryDate()).as("check expiryDate").isEqualTo(actual.getExpiryDate()))
            .satisfies(e -> assertThat(e.getCardholderName()).as("check cardholderName").isEqualTo(actual.getCardholderName()))
            .satisfies(e -> assertThat(e.getIsDefault()).as("check isDefault").isEqualTo(actual.getIsDefault()))
            .satisfies(e -> assertThat(e.getIsValid()).as("check isValid").isEqualTo(actual.getIsValid()));
    }

    protected void assertClientCardAllUpdatablePropertiesEquals(ClientCard expected, ClientCard actual) {
        assertThat(expected)
            .satisfies(e -> assertThat(e.getMaskedPan()).as("check maskedPan").isEqualTo(actual.getMaskedPan()))
            .satisfies(e -> assertThat(e.getExpiryDate()).as("check expiryDate").isEqualTo(actual.getExpiryDate()))
            .satisfies(e -> assertThat(e.getCardholderName()).as("check cardholderName").isEqualTo(actual.getCardholderName()))
            .satisfies(e -> assertThat(e.getIsDefault()).as("check isDefault").isEqualTo(actual.getIsDefault()))
            .satisfies(e -> assertThat(e.getIsValid()).as("check isValid").isEqualTo(actual.getIsValid()));
    }

    protected void partialUpdateClientCardWithPatch(ClientCard existingClientCard, Consumer<ClientCard> patch) throws Exception {
        ClientCard partialUpdatedClientCard = createUpdateProxyForBean(existingClientCard, existingClientCard);
        patch.accept(partialUpdatedClientCard);

        restClientCardMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedClientCard.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedClientCard))
            )
            .andExpect(status().isOk());
    }
}
