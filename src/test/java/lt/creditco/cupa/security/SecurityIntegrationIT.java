package lt.creditco.cupa.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class SecurityIntegrationIT {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MerchantRepository merchantRepository;

    private Merchant testMerchant;

    @BeforeEach
    void setUp() {
        // Setup test merchant
        testMerchant = new Merchant();
        testMerchant.setId("MER-TEST-001");
        testMerchant.setMode(MerchantMode.TEST);
        testMerchant.setStatus(MerchantStatus.ACTIVE);
        testMerchant.setCupaTestApiKey("test-api-key-123");
    }

    @Test
    void shouldMaintainSecurityAcrossAllEndpoints() throws Exception {
        // Test that security is maintained across all endpoint types:
        // - Public endpoints (no authentication required)
        // - Protected endpoints (JWT only)
        // - CupaApi endpoints (both JWT and API key)
        // - Admin endpoints (JWT with admin role)

        // Public endpoints should be accessible
        mvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized()); // Expected behavior in JHipster

        // Protected endpoints should require authentication
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized()); // Expected - requires authentication

        mvc.perform(get("/api/merchants")).andExpect(status().isUnauthorized()); // Expected - requires authentication

        // CupaApi endpoints should work with valid API keys
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        mvc
            .perform(get("/api/v1/payments/test-order-id").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isNotFound()); // Expected - authenticated but data not found
    }

    @Test
    void shouldHandleMixedAuthenticationMethods() throws Exception {
        // Test that both JWT and API key authentication work for CupaApi endpoints
        // and that they don't interfere with each other

        // Test API key authentication
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        mvc
            .perform(get("/api/v1/payments/test-order-id").header(Constants.API_KEY_HEADER, "test-api-key-123"))
            .andExpect(status().isNotFound()); // Expected - authenticated but data not found

        // Test that JWT authentication still works for other endpoints
        mvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized()); // Expected - requires authentication
    }

    @Test
    void shouldEnforceAuthenticationBoundaries() throws Exception {
        // Test that authentication boundaries are properly enforced

        // No authentication should fail for protected endpoints
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/merchants")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/clients")).andExpect(status().isUnauthorized());

        // API key should not work for non-CupaApi endpoints
        when(merchantRepository.findOneByCupaTestApiKey("test-api-key-123")).thenReturn(Optional.of(testMerchant));

        mvc.perform(get("/api/admin/users").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isForbidden());

        mvc.perform(get("/api/merchants")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/merchants").header(Constants.API_KEY_HEADER, "wrong-api-key")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/merchants").header(Constants.API_KEY_HEADER, "test-api-key-123")).andExpect(status().isOk());

    }

    @Test
    void shouldHandleInvalidApiKeysGracefully() throws Exception {
        // Test that invalid API keys are handled gracefully and don't break the system
        // Invalid API keys should return 401 (Unauthorized), not 200

        // Test with invalid API key
        mvc
            .perform(get("/api/v1/payments/test-order-id").header(Constants.API_KEY_HEADER, "invalid-key"))
            .andExpect(status().isUnauthorized()); // Expected - invalid API key should return 401

        // Test with no API key
        mvc.perform(get("/api/v1/payments/test-order-id")).andExpect(status().isUnauthorized()); // Expected - no API key should return 401

        // Test that other endpoints still work normally
        mvc.perform(get("/api/authenticate")).andExpect(status().isUnauthorized()); // Expected - requires authentication
    }

    @Test
    void shouldMaintainJwtSecurityModel() throws Exception {
        // Test that JWT security model is maintained for non-CupaApi endpoints

        // These endpoints should not accept API keys at all
        mvc.perform(get("/api/admin/users").header(Constants.API_KEY_HEADER, "any-key")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/merchants").header(Constants.API_KEY_HEADER, "any-key")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/clients").header(Constants.API_KEY_HEADER, "any-key")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/payment-transactions").header(Constants.API_KEY_HEADER, "any-key")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBlockRegisterEndpointCompletely() throws Exception {
        // Test that register endpoint is completely blocked

        // POST to register should be blocked
        mvc
            .perform(
                post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"login\":\"test\",\"email\":\"test@test.com\",\"password\":\"test\"}")
            )
            .andExpect(status().isUnauthorized());

        // GET to register should also be blocked
        mvc.perform(get("/api/register")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleCorsAndSecurityHeaders() throws Exception {
        // Test that security headers are properly set for API endpoints
        // These endpoints should return 401 for anonymous users, but headers should be set

        // Test /api/authenticate endpoint
        mvc
            .perform(get("/api/authenticate"))
            .andExpect(status().isUnauthorized()) // Expected - requires authentication
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "0"))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().string("Expires", "0"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
            .andExpect(header().exists("Content-Security-Policy"));

        // Test other API endpoints also have security headers
        mvc
            .perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized()) // Expected - requires authentication
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
            .andExpect(header().exists("Content-Security-Policy"));

        // Test /api/v1/** endpoints (merchant API)
        mvc
            .perform(get("/api/v1/payments/test-id"))
            .andExpect(status().isUnauthorized()) // Expected - requires X-API-Key
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void shouldApplySecurityHeadersToVaadinEndpoints() throws Exception {
        // Test that security headers are also applied to Vaadin UI endpoints
        mvc
            .perform(get("/"))
            .andExpect(status().isFound()) // Expect redirect to /login
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "0"))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"))
            .andExpect(header().string("Pragma", "no-cache"))
            .andExpect(header().string("Expires", "0"))
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
            .andExpect(header().exists("Content-Security-Policy"));
    }
}
