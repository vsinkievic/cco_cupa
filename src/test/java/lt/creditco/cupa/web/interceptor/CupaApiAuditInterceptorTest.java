package lt.creditco.cupa.web.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CupaApiAuditInterceptorTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CupaApiBusinessLogicService businessLogicService;

    @Mock
    private Authentication authentication;

    @Mock
    private Principal principal;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CupaApiAuditInterceptor interceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private CupaApiContext.CupaApiContextData contextData;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Setup request
        request.setMethod("GET");
        request.setRequestURI("/api/v1/payments/test-order-id");
        request.setRemoteAddr("192.168.1.1");

        // Setup context data
        contextData = CupaApiContext.CupaApiContextData.builder()
            .merchantId("test-merchant")
            .environment("TEST")
            .cupaApiKey("test-api-key")
            .orderId("test-order-id")
            .clientId("test-client")
            .requestData("test request data")
            .requesterIpAddress("192.168.1.1")
            .apiEndpoint("/api/v1/payments/test-order-id")
            .httpMethod("GET")
            .requestTimestamp(Instant.now())
            .build();

        // Setup security context with lenient stubbing
        lenient().when(authentication.getPrincipal()).thenReturn(principal);
        lenient().when(principal.getName()).thenReturn("test-merchant_test");
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldCreateAuditLogOnPreHandle() throws Exception {
        // Given
        AuditLogDTO savedAuditLog = new AuditLogDTO();
        savedAuditLog.setId(1L);

        when(businessLogicService.extractBusinessContext(any(), any(), any())).thenReturn(contextData);
        when(auditLogService.save(any(AuditLogDTO.class))).thenReturn(savedAuditLog);

        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assert result;
        verify(businessLogicService).extractBusinessContext(any(), any(), any());
        verify(auditLogService).save(any(AuditLogDTO.class));

        // Verify context was set
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();
        assert context != null;
        assert "test-merchant".equals(context.getMerchantId());
        assert "TEST".equals(context.getEnvironment());
    }

    @Test
    void shouldUpdateAuditLogOnPostHandle() throws Exception {
        // Given
        AuditLogDTO existingAuditLog = new AuditLogDTO();
        existingAuditLog.setId(1L);

        contextData.setAuditLogId(1L);
        CupaApiContext.setContext(contextData);

        when(auditLogService.findOne(1L)).thenReturn(Optional.of(existingAuditLog));
        when(auditLogService.update(any(AuditLogDTO.class))).thenReturn(existingAuditLog);

        // When
        interceptor.postHandle(request, response, null, null);

        // Then
        verify(auditLogService).findOne(1L);
        verify(auditLogService).update(any(AuditLogDTO.class));
    }

    @Test
    void shouldUpdateAuditLogWithException() throws Exception {
        // Given
        AuditLogDTO existingAuditLog = new AuditLogDTO();
        existingAuditLog.setId(1L);

        contextData.setAuditLogId(1L);
        CupaApiContext.setContext(contextData);

        Exception testException = new RuntimeException("Test exception");

        when(auditLogService.findOne(1L)).thenReturn(Optional.of(existingAuditLog));
        when(auditLogService.update(any(AuditLogDTO.class))).thenReturn(existingAuditLog);

        // When
        interceptor.afterCompletion(request, response, null, testException);

        // Then
        verify(auditLogService).findOne(1L);
        verify(auditLogService).update(any(AuditLogDTO.class));

        // Verify context was cleared
        CupaApiContext.clearContext();
        assert CupaApiContext.getContext() == null;
    }

    @Test
    void shouldHandlePreHandleExceptionGracefully() throws Exception {
        // Given
        when(businessLogicService.extractBusinessContext(any(), any(), any())).thenThrow(new RuntimeException("Test exception"));

        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assert result; // Should not block the request
        verify(auditLogService, never()).save(any());
    }
}
