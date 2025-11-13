package lt.creditco.cupa.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.AuditLog;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.AuditLogRepository;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class ApiKeyAuthenticationIT {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MerchantRepository merchantRepository;

    @MockBean
    private PaymentTransactionService paymentTransactionService;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private AuditLogRepository auditLogRepository;

    private Merchant testMerchant;
    private Merchant liveMerchant;
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

        // Setup live merchant
        liveMerchant = new Merchant();
        liveMerchant.setId("MER-LIVE-001");
        liveMerchant.setMode(MerchantMode.LIVE);
        liveMerchant.setStatus(MerchantStatus.ACTIVE);
        liveMerchant.setCupaProdApiKey("live-api-key-456");

        // Setup mock payment transaction for test merchant
        mockPaymentTransaction = new PaymentTransactionDTO();
        mockPaymentTransaction.setId("payment-123");
        mockPaymentTransaction.setOrderId("order-123");
        mockPaymentTransaction.setMerchantId("MER-TEST-001");

        // Setup mock payment
        mockPayment = new Payment();
        mockPayment.setId("payment-123");
        mockPayment.setOrderId("order-123");

        // Setup default mocks
        when(paymentTransactionService.findOne("payment-123")).thenReturn(Optional.of(mockPaymentTransaction));
        when(paymentTransactionService.findByMerchantIdAndOrderId("MER-TEST-001", "order-123")).thenReturn(
            Optional.of(mockPaymentTransaction)
        );
        when(paymentMapper.toPayment(mockPaymentTransaction)).thenReturn(mockPayment);

        // Mock AuditLogRepository.save() to simulate database ID generation
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLog.setId(1L); // Simulate database-generated ID
            return auditLog;
        });
    }

    @Test
    void shouldAllowAccessToCupaApiWithValidTestApiKey() throws Exception {
        // Given: Valid test API key for active merchant
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with test API key
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "0"))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldAllowAccessToCupaApiWithValidLiveApiKey() throws Exception {
        // Given: Valid live API key for active merchant
        when(merchantRepository.findOneByCupaProdApiKey("live-api-key-456")).thenReturn(Optional.of(liveMerchant));

        // Create separate mock data for live merchant
        PaymentTransactionDTO livePaymentTransaction = new PaymentTransactionDTO();
        livePaymentTransaction.setId("payment-456");
        livePaymentTransaction.setOrderId("order-456");
        livePaymentTransaction.setMerchantId("MER-LIVE-001");

        Payment livePayment = new Payment();
        livePayment.setId("payment-456");
        livePayment.setOrderId("order-456");

        when(paymentTransactionService.findOne("payment-456")).thenReturn(Optional.of(livePaymentTransaction));
        when(paymentMapper.toPayment(livePaymentTransaction)).thenReturn(livePayment);

        // When: Access CupaApiResource with live API key
        mvc
            .perform(get("/api/v1/payments/payment-456").header(Constants.API_KEY_HEADER, "live-api-key-456"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldDenyAccessToCupaApiWithInvalidApiKey() throws Exception {
        // Given: Invalid API key
        when(merchantRepository.findOneByCupaTestApiKey(anyString())).thenReturn(Optional.empty());
        when(merchantRepository.findOneByCupaProdApiKey(anyString())).thenReturn(Optional.empty());

        // When: Access CupaApiResource with invalid API key
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "invalid-api-key"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldDenyAccessToOtherAdminResourcesWithInvalidApiKey() throws Exception {
        // Given: Invalid API key
        when(merchantRepository.findOneByCupaTestApiKey(anyString())).thenReturn(Optional.empty());
        when(merchantRepository.findOneByCupaProdApiKey(anyString())).thenReturn(Optional.empty());

        // When: Access CupaApiResource with invalid API key
        mvc
            .perform(get("/api/admin/users").header(Constants.API_KEY_HEADER, "invalid-api-key"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldDenyAccessToCupaApiWithoutApiKey() throws Exception {
        // When: Access CupaApiResource without API key
        mvc.perform(get("/api/v1/payments/payment-123")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessToCupaApiWithInactiveMerchant() throws Exception {
        // Given: Valid API key but inactive merchant
        testMerchant.setStatus(MerchantStatus.INACTIVE);
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with API key for inactive merchant
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessToCupaApiWithModeMismatch() throws Exception {
        // Given: Test API key but merchant is in LIVE mode
        testMerchant.setMode(MerchantMode.LIVE);
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with test API key for live merchant
        mvc
            .perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToOtherAdminResourcesWithValidApiKey() throws Exception {
        // Given: Valid API key
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Try to access non-CupaApiResource with API key
        mvc.perform(get("/api/admin/users").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isForbidden()); // Should accept API key
    }

    @Test
    void shouldDenyAccessToUiResourcesWithApiKey() throws Exception {
        // Given: Valid API key
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Try to access non-CupaApiResource with API key
        // Then: Should redirect to login (API key is not accepted for UI resources)
        mvc
            .perform(get("/ui/admin/users").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void shouldAllowAccessToCupaApiWithCorrectModeAndStatus() throws Exception {
        // Given: Test API key for test merchant with active status
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access CupaApiResource with matching mode and status
        mvc.perform(get("/api/v1/payments/payment-123").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToMerchantSpecificEndpoint() throws Exception {
        // Given: Valid API key for merchant-specific endpoint
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        // When: Access merchant-specific endpoint with API key
        mvc
            .perform(get("/api/v1/merchants/MER-TEST-001/payments/order-123").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isOk());
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

}
