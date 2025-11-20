package lt.creditco.cupa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.base.users.CupaUserRepository;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.PaymentCurrency;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class CupaApiBusinessLogicServiceTest {

    @Mock
    private MerchantService merchantService;

    @Mock
    private CupaUserRepository userRepo;

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Principal principal;

    @InjectMocks
    private CupaApiBusinessLogicService businessLogicService;

    private MockHttpServletRequest request;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/v1/payments");
        request.setRemoteAddr("192.168.1.1");

        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("test-order-123");
        paymentRequest.setClientId("test-client-456");
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency(PaymentCurrency.USD);
        paymentRequest.setCardType(CardType.UnionPay);
        paymentRequest.setPaymentFlow(PaymentFlow.EMAIL);
    }

    @Test
    void shouldExtractBusinessContextFromPaymentRequest() throws Exception {
        // Given
        request.addHeader("X-API-Key", "test-api-key");
        when(principal.getName()).thenReturn("test-merchant_test");

        CupaUser user = new CupaUser();
        user.setLogin("test-merchant_test");
        user.setMerchantIds("test-merchant");

        Merchant merchant = new Merchant();
        merchant.setId("test-merchant");
        merchant.setCupaTestApiKey("test-api-key");
        merchant.setMode(MerchantMode.TEST);
        merchant.setStatus(MerchantStatus.ACTIVE);

        when(userRepo.findOneWithAuthoritiesByLogin("test-merchant_test")).thenReturn(Optional.of(user));
        when(merchantService.findMerchantByCupaApiKey("test-api-key")).thenReturn(merchant);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"test-order-123\"}");

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentRequest, principal);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getOrderId()).isEqualTo("test-order-123");
        assertThat(context.getClientId()).isEqualTo("test-client-456");
        assertThat(context.getMerchantId()).isEqualTo("test-merchant");
        assertThat(context.getEnvironment()).isEqualTo(MerchantMode.TEST);
        assertThat(context.getCupaApiKey()).isEqualTo("test-api-key");
        assertThat(context.getApiEndpoint()).isEqualTo("/api/v1/payments");
        assertThat(context.getHttpMethod()).isEqualTo("POST");
        assertThat(context.getRequesterIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldExtractOrderIdFromPathVariables() throws Exception {
        // Given
        request.setRequestURI("/api/v1/payments/orderId/test-order-from-path");
        request.setPathInfo("/orderId/test-order-from-path");
        request.addHeader("X-API-Key", "test-api-key");
        when(principal.getName()).thenReturn("test-merchant_test");

        CupaUser user = new CupaUser();
        user.setLogin("test-merchant_test");
        user.setMerchantIds("test-merchant");

        Merchant merchant = new Merchant();
        merchant.setId("test-merchant");
        merchant.setCupaTestApiKey("test-api-key");
        merchant.setMode(MerchantMode.TEST);
        merchant.setStatus(MerchantStatus.ACTIVE);

        when(userRepo.findOneWithAuthoritiesByLogin("test-merchant_test")).thenReturn(Optional.of(user));
        when(merchantService.findMerchantByCupaApiKey("test-api-key")).thenReturn(merchant);

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, null, principal);

        // Then
        assertThat(context.getOrderId()).isEqualTo("test-order-from-path");
    }

    @Test
    void shouldHandleNullPrincipal() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentRequest, null);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getMerchantId()).isNull();
        assertThat(context.getEnvironment()).isNull();
        assertThat(context.getCupaApiKey()).isNull();
    }

    @Test
    void shouldHandleMerchantNotFound() throws Exception {
        // Given
        when(principal.getName()).thenReturn("unknown-merchant_test");

        CupaUser user = new CupaUser();
        user.setLogin("unknown-merchant_test");
        user.setMerchantIds("unknown-merchant");

        when(userRepo.findOneWithAuthoritiesByLogin("unknown-merchant_test")).thenReturn(Optional.of(user));
        when(merchantService.findMerchantById("unknown-merchant")).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentRequest, principal);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getMerchantId()).isNull();
        assertThat(context.getEnvironment()).isNull();
        assertThat(context.getCupaApiKey()).isNull();
    }

    @Test
    void shouldHandleLiveEnvironment() throws Exception {
        // Given
        request.addHeader("X-API-Key", "live-api-key");
        when(principal.getName()).thenReturn("test-merchant_live");

        CupaUser user = new CupaUser();
        user.setLogin("test-merchant_live");
        user.setMerchantIds("test-merchant");

        Merchant merchant = new Merchant();
        merchant.setId("test-merchant");
        merchant.setCupaProdApiKey("live-api-key");
        merchant.setMode(MerchantMode.LIVE);
        merchant.setStatus(MerchantStatus.ACTIVE);

        when(userRepo.findOneWithAuthoritiesByLogin("test-merchant_live")).thenReturn(Optional.of(user));
        when(merchantService.findMerchantByCupaApiKey("live-api-key")).thenReturn(merchant);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentRequest, principal);

        // Then
        assertThat(context.getEnvironment()).isEqualTo(MerchantMode.LIVE);
        assertThat(context.getCupaApiKey()).isEqualTo("live-api-key");
    }

    @Test
    void shouldHandleRequestDataExtractionError() throws Exception {
        // Given
        request.addHeader("X-API-Key", "test-api-key");
        when(principal.getName()).thenReturn("test-merchant_test");
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Don't investigate this message in logs. This excptions was thrown to test the error handling in ObjectMapper.writeValueAsString() method. JSON error"));

        CupaUser user = new CupaUser();
        user.setLogin("test-merchant_test");
        user.setMerchantIds("test-merchant");

        Merchant merchant = new Merchant();
        merchant.setId("test-merchant");
        merchant.setCupaTestApiKey("test-api-key");
        merchant.setMode(MerchantMode.TEST);
        merchant.setStatus(MerchantStatus.ACTIVE);

        when(userRepo.findOneWithAuthoritiesByLogin("test-merchant_test")).thenReturn(Optional.of(user));
        when(merchantService.findMerchantByCupaApiKey("test-api-key")).thenReturn(merchant);

        // When
        CupaApiContext.CupaApiContextData context = businessLogicService.extractBusinessContext(request, paymentRequest, principal);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getRequestData()).contains("Error extracting request data");
    }
}
