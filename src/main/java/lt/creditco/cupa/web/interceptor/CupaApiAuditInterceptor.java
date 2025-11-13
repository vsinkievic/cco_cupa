package lt.creditco.cupa.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor for auditing CUPA API requests.
 * Captures request/response data and creates audit log entries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CupaApiAuditInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;
    private final CupaApiBusinessLogicService businessLogicService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // The context should have been created by ApiKeyAuthenticationFilter.
            CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();
            Object requestBody = null;
            boolean triedToExtractRequestBody = false;
            if (context == null) {
                // This is unexpected for API requests and may indicate a configuration issue
                // where ApiKeyAuthenticationFilter did not run.
                // For robustness, we can create it here as a fallback.
                log.warn("CupaApiContext not found, creating in interceptor as fallback for request: {}", request.getRequestURI());

                requestBody = extractRequestBody(request);
                triedToExtractRequestBody = true;
                Principal principal = getCurrentPrincipal();
                context = businessLogicService.extractBusinessContext(request, requestBody, principal);
                CupaApiContext.setContext(context);
            }
            if (context != null && context.getRequestData() == null && !triedToExtractRequestBody){
                requestBody = extractRequestBody(request);
                if (requestBody != null){
                    context.setRequestData(objectMapper.writeValueAsString(requestBody));
                    CupaApiContext.setContext(context);
                }
            }

            // Create initial audit log entry
            AuditLogDTO auditLog = createInitialAuditLog(context);
            AuditLogDTO savedLog = auditLogService.save(auditLog);

            response.setHeader("X-Response-Id", savedLog.getId().toString());

            // Store audit log ID in context
            if (context != null){
                context.setAuditLogId(savedLog.getId());
                // No need to call CupaApiContext.setContext(context) again if it was already set.
                // However, it is safe to do so as it just sets the same object again.
                CupaApiContext.setContext(context);
            }

            log.debug(
                "Created audit log entry with ID: {} for request: {} {}",
                savedLog.getId(),
                request.getMethod(),
                request.getRequestURI()
            );

            return true;
        } catch (Throwable e) {
            log.error("Error in audit interceptor preHandle", e);
            return true; // Don't block the request
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //       updateAuditLogWithResponse(response);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (ex != null) {
                updateAuditLogWithException(ex);
            } else {
                // Capture response for all scenarios (200, 400, 500, etc.)
                //     updateAuditLogWithResponse(response);
            }
        } finally {
            // Always clear the context
            CupaApiContext.clearContext();
        }
    }

    private Object extractRequestBody(HttpServletRequest request) {
        // For now, we'll return null as extracting request body from HttpServletRequest
        // requires a wrapper. In a real implementation, you might use a request wrapper
        // or extract the body from the handler method parameters.
        return null;
    }

    private Principal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Principal) {
            return (Principal) authentication.getPrincipal();
        }
        return authentication;
    }

    private AuditLogDTO createInitialAuditLog(CupaApiContext.CupaApiContextData context) {
        AuditLogDTO auditLog = new AuditLogDTO();
        auditLog.setRequestTimestamp(context.getRequestTimestamp());
        auditLog.setApiEndpoint(context.getApiEndpoint());
        auditLog.setHttpMethod(context.getHttpMethod());
        auditLog.setRequesterIpAddress(context.getRequesterIpAddress());
        auditLog.setRequestData(context.getRequestData());
        auditLog.setOrderId(context.getOrderId());
        auditLog.setMerchantId(context.getMerchantId());
        if (context.getMerchantContext() != null && context.getMerchantContext().getMode() != null) {
            auditLog.setEnvironment(context.getMerchantContext().getMode().name());
        }
        auditLog.setCupaApiKey(context.getCupaApiKey());

        return auditLog;
    }

    private void updateAuditLogWithResponse(HttpServletResponse response) {
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();
        if (context != null && context.getAuditLogId() != null) {
            try {
                AuditLogDTO auditLog = auditLogService.findOne(context.getAuditLogId()).orElse(null);
                if (auditLog != null) {
                    auditLog.setHttpStatusCode(response.getStatus());
                    //                   auditLog.setResponseDescription(getResponseDescription(response.getStatus()));

                    // Note: Response body capture would require a response wrapper
                    // This is a simplified version
                    auditLogService.update(auditLog);

                    log.debug("Updated audit log entry {} with response status: {}", context.getAuditLogId(), response.getStatus());
                }
            } catch (Exception e) {
                log.error("Error updating audit log with response", e);
            }
        }
    }

    private void updateAuditLogWithException(Exception ex) {
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();
        if (context != null && context.getAuditLogId() != null) {
            try {
                AuditLogDTO auditLog = auditLogService.findOne(context.getAuditLogId()).orElse(null);
                if (auditLog != null) {
                    auditLog.setHttpStatusCode(500);
                    auditLog.setResponseDescription("Exception occurred: " + ex.getMessage());
                    auditLog.setResponseData(ex.toString());
                    auditLogService.update(auditLog);

                    log.debug("Updated audit log entry {} with exception: {}", context.getAuditLogId(), ex.getMessage());
                }
            } catch (Exception e) {
                log.error("Error updating audit log with exception", e);
            }
        }
    }
}
