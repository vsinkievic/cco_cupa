package lt.creditco.cupa.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.base.users.CupaUserRepository;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.bpmid.vapp.domain.Authority;
import com.bpmid.vapp.security.AuthoritiesConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
class CupaApiResourceSecurityIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MerchantRepository merchantRepository;

    @MockitoBean
    private PaymentTransactionService paymentTransactionService;

    @MockitoBean
    private PaymentMapper paymentMapper;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private CupaUserRepository cupaUserRepository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public MerchantService merchantService(MerchantRepository merchantRepository, MerchantMapper merchantMapper) {
            Objects.requireNonNull(merchantRepository, "merchantRepository must be set");
            Objects.requireNonNull(merchantMapper, "merchantMapper must be set");
            return new MerchantService(merchantRepository, merchantMapper);
        }

        @Bean
        public CupaApiBusinessLogicService cupaApiBusinessLogicService(
            MerchantService merchantService,
            CupaUserRepository cupaUserRepository,
            ObjectMapper objectMapper
        ) {
            Objects.requireNonNull(merchantService, "merchantService must be set");
            Objects.requireNonNull(cupaUserRepository, "cupaUserRepository must be set");
            Objects.requireNonNull(objectMapper, "objectMapper must be set");
            return new CupaApiBusinessLogicService(merchantService, cupaUserRepository, objectMapper);
        }
    }


    private Merchant testMerchant;
    private PaymentTransactionDTO mockPaymentTransaction;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        // Setup test merchant
        testMerchant = new Merchant();
        testMerchant.setId("MER-TEST-001");
        testMerchant.setMode(MerchantMode.TEST);
        testMerchant.setStatus(MerchantStatus.ACTIVE);
        testMerchant.setCupaTestApiKey("test-api-key-123");

        // Setup mock payment transaction
        mockPaymentTransaction = new PaymentTransactionDTO();
        mockPaymentTransaction.setId("payment-123");
        mockPaymentTransaction.setOrderId("order-123");
        mockPaymentTransaction.setMerchantId("MER-TEST-001");

        // Setup mock payment
        mockPayment = new Payment();
        mockPayment.setId("payment-123");
        mockPayment.setOrderId("order-123");

        // Mock AuditLogRepository.save() to simulate database ID generation
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLog.setId(1L); // Simulate database-generated ID
            return auditLog;
        });
    }

    @Test
    @WithMockUser(username = "admin", authorities = AuthoritiesConstants.ADMIN)
    void shouldAllowAccessWithAdminAuthorityAndTheSameMerchantIdSet() throws Exception {
        // given
        CupaUser adminUser = new CupaUser();
        adminUser.setLogin("admin");
        adminUser.setMerchantIds("MER-TEST-001");
        adminUser.setAuthorities(Set.of(new Authority().name(AuthoritiesConstants.ADMIN)));
        when(cupaUserRepository.findOneWithAuthoritiesByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(merchantRepository.findById("MER-TEST-001")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        
        // When: Access CupaApiResource with admin authority
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", authorities = AuthoritiesConstants.ADMIN)
    void shouldDenyAccessWithAdminAuthorityAndAnFewMerchantIdSet() throws Exception {
        // given
        CupaUser adminUser = new CupaUser();
        adminUser.setLogin("admin");
        adminUser.setMerchantIds("MER-TEST-001,MER-TEST-002");
        adminUser.setAuthorities(Set.of(new Authority().name(AuthoritiesConstants.ADMIN)));
        when(cupaUserRepository.findOneWithAuthoritiesByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(merchantRepository.findById("MER-TEST-001")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        
        // When: Access CupaApiResource with admin authority
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = AuthoritiesConstants.ADMIN)
    void shouldDenyAccessWithAdminAuthorityAndWrongMerchantIdSet() throws Exception {
        // given
        CupaUser adminUser = new CupaUser();
        adminUser.setLogin("admin");
        adminUser.setMerchantIds("MER-TEST-002");
        adminUser.setAuthorities(Set.of(new Authority().name(AuthoritiesConstants.ADMIN)));
        when(cupaUserRepository.findOneWithAuthoritiesByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(merchantRepository.findById("MER-TEST-001")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        
        // When: Access CupaApiResource with admin authority
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", authorities = AuthoritiesConstants.USER)
    void shouldAllowAccessWithUserAuthorityAndTheSameMerchantIdSet() throws Exception {
        // given
        CupaUser user = new CupaUser();
        user.setLogin("user");
        user.setMerchantIds("MER-TEST-001");
        user.setAuthorities(Set.of(new Authority().name(AuthoritiesConstants.USER)));
        when(cupaUserRepository.findOneWithAuthoritiesByLogin("user")).thenReturn(Optional.of(user));
        when(merchantRepository.findById("MER-TEST-001")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        
        // When: Access CupaApiResource with admin authority
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessWithValidApiKey() throws Exception {
        // Given: Valid API key and mock service responses
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        when(paymentMapper.toPayment(mockPaymentTransaction)).thenReturn(mockPayment);

        // When: Access CupaApiResource with API key
        mvc.perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        // When: Access CupaApiResource without authentication
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithInvalidApiKey() throws Exception {
        // Given: Invalid API key
        when(merchantRepository.findOneByCupaTestApiKey("invalid-key")).thenReturn(Optional.empty());
        when(merchantRepository.findOneByCupaProdApiKey("invalid-key")).thenReturn(Optional.empty());

        // When: Access CupaApiResource with invalid API key
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "invalid-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithInactiveMerchant() throws Exception {
        // Given: Valid API key but inactive merchant
        testMerchant.setStatus(MerchantStatus.INACTIVE);
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with API key for inactive merchant
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithModeMismatch() throws Exception {
        // Given: Test API key but merchant is in LIVE mode
        testMerchant.setMode(MerchantMode.LIVE);
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with test API key for live merchant
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToMerchantSpecificEndpoint() throws Exception {
        // Given: Valid API key and mock service responses for merchant-specific endpoint
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findByMerchantIdAndOrderId("MER-TEST-001", "order-123")).thenReturn(
            Optional.of(mockPaymentTransaction)
        );
        when(paymentMapper.toPayment(mockPaymentTransaction)).thenReturn(mockPayment);

        // When: Access merchant-specific endpoint with API key
        mvc
            .perform(get("/api/v1/merchants/MER-TEST-001/payments/order-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldEnforceMerchantAccessControl() throws Exception {
        // Given: Valid API key but trying to access different merchant's data
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findByMerchantIdAndOrderId("DIFFERENT-MERCHANT", "order-123")).thenReturn(Optional.empty());

        // When: Try to access different merchant's data
        mvc
            .perform(get("/api/v1/merchants/DIFFERENT-MERCHANT/payments/order-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isNotFound()); // Should not find the resource
    }

    @Test
    void shouldHandleMissingPaymentGracefully() throws Exception {
        // Given: Valid API key but payment not found
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("non-existent-payment")).thenReturn(Optional.empty());

        // When: Access non-existent payment
        mvc
            .perform(get("/api/v1/payments/non-existent-payment").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldAcceptBothAuthMethodsForSameEndpoint() throws Exception {
        // Given: Valid API key and mock service responses
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        when(paymentMapper.toPayment(mockPaymentTransaction)).thenReturn(mockPayment);

        // When: Access with API key
        mvc.perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isOk());
        // Note: JWT authentication would be tested with @WithMockUser in a real scenario
        // This test ensures that API key authentication works correctly
    }
}
