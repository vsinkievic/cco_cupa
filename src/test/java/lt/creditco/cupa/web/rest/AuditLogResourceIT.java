package lt.creditco.cupa.web.rest;

import static lt.creditco.cupa.domain.AuditLogAsserts.*;
import static lt.creditco.cupa.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import lt.creditco.cupa.IntegrationTest;
import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.service.mapper.AuditLogMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AuditLogResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AuditLogResourceIT {

    private static final Instant DEFAULT_REQUEST_TIMESTAMP = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_REQUEST_TIMESTAMP = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_API_ENDPOINT = "AAAAAAAAAA";
    private static final String UPDATED_API_ENDPOINT = "BBBBBBBBBB";

    private static final String DEFAULT_HTTP_METHOD = "AAAAAAAAAA";
    private static final String UPDATED_HTTP_METHOD = "BBBBBBBBBB";

    private static final Integer DEFAULT_HTTP_STATUS_CODE = 1;
    private static final Integer UPDATED_HTTP_STATUS_CODE = 2;

    private static final String DEFAULT_ORDER_ID = "AAAAAAAAAA";
    private static final String UPDATED_ORDER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_RESPONSE_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_RESPONSE_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_CUPA_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_CUPA_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_ENVIRONMENT = "AAAAAAAAAA";
    private static final String UPDATED_ENVIRONMENT = "BBBBBBBBBB";

    private static final String DEFAULT_REQUEST_DATA = "AAAAAAAAAA";
    private static final String UPDATED_REQUEST_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_RESPONSE_DATA = "AAAAAAAAAA";
    private static final String UPDATED_RESPONSE_DATA = "BBBBBBBBBB";

    private static final String DEFAULT_REQUESTER_IP_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_REQUESTER_IP_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/audit-logs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogRepository auditLogRepositoryMock;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Mock
    private AuditLogService auditLogServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuditLogMockMvc;

    private AuditLog auditLog;

    private AuditLog insertedAuditLog;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuditLog createEntity() {
        return new AuditLog()
            .requestTimestamp(DEFAULT_REQUEST_TIMESTAMP)
            .apiEndpoint(DEFAULT_API_ENDPOINT)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .httpStatusCode(DEFAULT_HTTP_STATUS_CODE)
            .orderId(DEFAULT_ORDER_ID)
            .responseDescription(DEFAULT_RESPONSE_DESCRIPTION)
            .cupaApiKey(DEFAULT_CUPA_API_KEY)
            .environment(DEFAULT_ENVIRONMENT)
            .requestData(DEFAULT_REQUEST_DATA)
            .responseData(DEFAULT_RESPONSE_DATA)
            .requesterIpAddress(DEFAULT_REQUESTER_IP_ADDRESS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuditLog createUpdatedEntity() {
        return new AuditLog()
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .apiEndpoint(UPDATED_API_ENDPOINT)
            .httpMethod(UPDATED_HTTP_METHOD)
            .httpStatusCode(UPDATED_HTTP_STATUS_CODE)
            .orderId(UPDATED_ORDER_ID)
            .responseDescription(UPDATED_RESPONSE_DESCRIPTION)
            .cupaApiKey(UPDATED_CUPA_API_KEY)
            .environment(UPDATED_ENVIRONMENT)
            .requestData(UPDATED_REQUEST_DATA)
            .responseData(UPDATED_RESPONSE_DATA)
            .requesterIpAddress(UPDATED_REQUESTER_IP_ADDRESS);
    }

    @BeforeEach
    void initTest() {
        auditLog = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAuditLog != null) {
            auditLogRepository.delete(insertedAuditLog);
            insertedAuditLog = null;
        }
    }

    @Test
    @Transactional
    void createAuditLog() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);
        var returnedAuditLogDTO = om.readValue(
            restAuditLogMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AuditLogDTO.class
        );

        // Validate the AuditLog in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAuditLog = auditLogMapper.toEntity(returnedAuditLogDTO);
        assertAuditLogUpdatableFieldsEquals(returnedAuditLog, getPersistedAuditLog(returnedAuditLog));

        insertedAuditLog = returnedAuditLog;
    }

    @Test
    @Transactional
    void createAuditLogWithExistingId() throws Exception {
        // Create the AuditLog with an existing ID
        auditLog.setId(1L);
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuditLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRequestTimestampIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditLog.setRequestTimestamp(null);

        // Create the AuditLog, which fails.
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        restAuditLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkApiEndpointIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditLog.setApiEndpoint(null);

        // Create the AuditLog, which fails.
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        restAuditLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkHttpMethodIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        auditLog.setHttpMethod(null);

        // Create the AuditLog, which fails.
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        restAuditLogMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAuditLogs() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        // Get all the auditLogList
        restAuditLogMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auditLog.getId().intValue())))
            .andExpect(jsonPath("$.[*].requestTimestamp").value(hasItem(DEFAULT_REQUEST_TIMESTAMP.toString())))
            .andExpect(jsonPath("$.[*].apiEndpoint").value(hasItem(DEFAULT_API_ENDPOINT)))
            .andExpect(jsonPath("$.[*].httpMethod").value(hasItem(DEFAULT_HTTP_METHOD)))
            .andExpect(jsonPath("$.[*].httpStatusCode").value(hasItem(DEFAULT_HTTP_STATUS_CODE)))
            .andExpect(jsonPath("$.[*].orderId").value(hasItem(DEFAULT_ORDER_ID)))
            .andExpect(jsonPath("$.[*].responseDescription").value(hasItem(DEFAULT_RESPONSE_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].cupaApiKey").value(hasItem(DEFAULT_CUPA_API_KEY)))
            .andExpect(jsonPath("$.[*].environment").value(hasItem(DEFAULT_ENVIRONMENT)))
            .andExpect(jsonPath("$.[*].requestData").value(hasItem(DEFAULT_REQUEST_DATA)))
            .andExpect(jsonPath("$.[*].responseData").value(hasItem(DEFAULT_RESPONSE_DATA)))
            .andExpect(jsonPath("$.[*].requesterIpAddress").value(hasItem(DEFAULT_REQUESTER_IP_ADDRESS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAuditLogsWithEagerRelationshipsIsEnabled() throws Exception {
        when(auditLogServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAuditLogMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(auditLogServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAuditLogsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(auditLogServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAuditLogMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(auditLogRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAuditLog() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        // Get the auditLog
        restAuditLogMockMvc
            .perform(get(ENTITY_API_URL_ID, auditLog.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(auditLog.getId().intValue()))
            .andExpect(jsonPath("$.requestTimestamp").value(DEFAULT_REQUEST_TIMESTAMP.toString()))
            .andExpect(jsonPath("$.apiEndpoint").value(DEFAULT_API_ENDPOINT))
            .andExpect(jsonPath("$.httpMethod").value(DEFAULT_HTTP_METHOD))
            .andExpect(jsonPath("$.httpStatusCode").value(DEFAULT_HTTP_STATUS_CODE))
            .andExpect(jsonPath("$.orderId").value(DEFAULT_ORDER_ID))
            .andExpect(jsonPath("$.responseDescription").value(DEFAULT_RESPONSE_DESCRIPTION))
            .andExpect(jsonPath("$.cupaApiKey").value(DEFAULT_CUPA_API_KEY))
            .andExpect(jsonPath("$.environment").value(DEFAULT_ENVIRONMENT))
            .andExpect(jsonPath("$.requestData").value(DEFAULT_REQUEST_DATA))
            .andExpect(jsonPath("$.responseData").value(DEFAULT_RESPONSE_DATA))
            .andExpect(jsonPath("$.requesterIpAddress").value(DEFAULT_REQUESTER_IP_ADDRESS));
    }

    @Test
    @Transactional
    void getNonExistingAuditLog() throws Exception {
        // Get the auditLog
        restAuditLogMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAuditLog() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditLog
        AuditLog updatedAuditLog = auditLogRepository.findById(auditLog.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAuditLog are not directly saved in db
        em.detach(updatedAuditLog);
        updatedAuditLog
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .apiEndpoint(UPDATED_API_ENDPOINT)
            .httpMethod(UPDATED_HTTP_METHOD)
            .httpStatusCode(UPDATED_HTTP_STATUS_CODE)
            .orderId(UPDATED_ORDER_ID)
            .responseDescription(UPDATED_RESPONSE_DESCRIPTION)
            .cupaApiKey(UPDATED_CUPA_API_KEY)
            .environment(UPDATED_ENVIRONMENT)
            .requestData(UPDATED_REQUEST_DATA)
            .responseData(UPDATED_RESPONSE_DATA)
            .requesterIpAddress(UPDATED_REQUESTER_IP_ADDRESS);
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(updatedAuditLog);

        restAuditLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, auditLogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditLogDTO))
            )
            .andExpect(status().isOk());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAuditLogToMatchAllProperties(updatedAuditLog);
    }

    @Test
    @Transactional
    void putNonExistingAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, auditLogDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(auditLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAuditLogWithPatch() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditLog using partial update
        AuditLog partialUpdatedAuditLog = new AuditLog();
        partialUpdatedAuditLog.setId(auditLog.getId());

        partialUpdatedAuditLog
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .httpMethod(UPDATED_HTTP_METHOD)
            .orderId(UPDATED_ORDER_ID)
            .responseDescription(UPDATED_RESPONSE_DESCRIPTION)
            .responseData(UPDATED_RESPONSE_DATA)
            .requesterIpAddress(UPDATED_REQUESTER_IP_ADDRESS);

        restAuditLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuditLog.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuditLog))
            )
            .andExpect(status().isOk());

        // Validate the AuditLog in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuditLogUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAuditLog, auditLog), getPersistedAuditLog(auditLog));
    }

    @Test
    @Transactional
    void fullUpdateAuditLogWithPatch() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the auditLog using partial update
        AuditLog partialUpdatedAuditLog = new AuditLog();
        partialUpdatedAuditLog.setId(auditLog.getId());

        partialUpdatedAuditLog
            .requestTimestamp(UPDATED_REQUEST_TIMESTAMP)
            .apiEndpoint(UPDATED_API_ENDPOINT)
            .httpMethod(UPDATED_HTTP_METHOD)
            .httpStatusCode(UPDATED_HTTP_STATUS_CODE)
            .orderId(UPDATED_ORDER_ID)
            .responseDescription(UPDATED_RESPONSE_DESCRIPTION)
            .cupaApiKey(UPDATED_CUPA_API_KEY)
            .environment(UPDATED_ENVIRONMENT)
            .requestData(UPDATED_REQUEST_DATA)
            .responseData(UPDATED_RESPONSE_DATA)
            .requesterIpAddress(UPDATED_REQUESTER_IP_ADDRESS);

        restAuditLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAuditLog.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAuditLog))
            )
            .andExpect(status().isOk());

        // Validate the AuditLog in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAuditLogUpdatableFieldsEquals(partialUpdatedAuditLog, getPersistedAuditLog(partialUpdatedAuditLog));
    }

    @Test
    @Transactional
    void patchNonExistingAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, auditLogDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(auditLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(auditLogDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAuditLog() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        auditLog.setId(longCount.incrementAndGet());

        // Create the AuditLog
        AuditLogDTO auditLogDTO = auditLogMapper.toDto(auditLog);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAuditLogMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(auditLogDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AuditLog in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAuditLog() throws Exception {
        // Initialize the database
        insertedAuditLog = auditLogRepository.saveAndFlush(auditLog);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the auditLog
        restAuditLogMockMvc
            .perform(delete(ENTITY_API_URL_ID, auditLog.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return auditLogRepository.count();
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

    protected AuditLog getPersistedAuditLog(AuditLog auditLog) {
        return auditLogRepository.findById(auditLog.getId()).orElseThrow();
    }

    protected void assertPersistedAuditLogToMatchAllProperties(AuditLog expectedAuditLog) {
        assertAuditLogAllPropertiesEquals(expectedAuditLog, getPersistedAuditLog(expectedAuditLog));
    }

    protected void assertPersistedAuditLogToMatchUpdatableProperties(AuditLog expectedAuditLog) {
        assertAuditLogAllUpdatablePropertiesEquals(expectedAuditLog, getPersistedAuditLog(expectedAuditLog));
    }
}
