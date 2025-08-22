package lt.creditco.cupa.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import lt.creditco.cupa.remote.PaymentReply;
import lt.creditco.cupa.service.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for processing payment gateway webhooks.
 * This endpoint is publicly accessible and processes payment notifications
 * from the payment gateway without requiring authentication.
 *
 * Supported parameters based on documentation:
 * - currency: Payment currency (e.g., AUD)
 * - success: Success status (Y/N)
 * - merchantID/merchantId: Merchant identifier (case insensitive)
 * - orderID/orderId: Order identifier (case insensitive)
 * - clientID/clientId: Client identifier (case insensitive)
 * - amount: Payment amount (for successful transactions)
 * - signature: Signature for verification
 * - detail: Transaction detail (for failed transactions)
 */
@RestController
@RequestMapping("/public")
public class WebhookController {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookController.class);

    private final PaymentTransactionService paymentTransactionService;

    public WebhookController(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    /**
     * Process webhook notification via GET request.
     * Maps query parameters to PaymentReply object and processes the notification.
     * Supports case insensitive parameters for merchantID/merchantId, orderID/orderId, clientID/clientId.
     *
     * @param request the HTTP request
     * @param currency the payment currency
     * @param success the success status (Y/N)
     * @param merchantID the merchant ID (original case)
     * @param merchantId the merchant ID (camelCase)
     * @param orderID the order ID (original case)
     * @param orderId the order ID (camelCase)
     * @param clientID the client ID (original case)
     * @param clientId the client ID (camelCase)
     * @param amount the payment amount (for successful transactions)
     * @param signature the signature for verification
     * @param detail the transaction detail (for failed transactions)
     * @return HTTP response
     */
    @GetMapping("/webhook")
    public ResponseEntity<Void> processWebhook(
        HttpServletRequest request,
        @RequestParam(value = "currency", required = false) String currency,
        @RequestParam(value = "success", required = false) String success,
        @RequestParam(value = "merchantID", required = false) String merchantID,
        @RequestParam(value = "merchantId", required = false) String merchantId,
        @RequestParam(value = "orderID", required = false) String orderID,
        @RequestParam(value = "orderId", required = false) String orderId,
        @RequestParam(value = "clientID", required = false) String clientID,
        @RequestParam(value = "clientId", required = false) String clientId,
        @RequestParam(value = "amount", required = false) String amount,
        @RequestParam(value = "signature", required = false) String signature,
        @RequestParam(value = "detail", required = false) String detail
    ) {
        String remoteIp = getRemoteIpAddress(request);

        // Handle case insensitive parameters - prefer original case (merchantID, orderID, clientID)
        String finalMerchantId = merchantID != null ? merchantID : merchantId;
        String finalOrderId = orderID != null ? orderID : orderId;
        String finalClientId = clientID != null ? clientID : clientId;

        // Silent exit if required fields are missing
        if (finalMerchantId == null || finalOrderId == null || signature == null) {
            return ResponseEntity.ok().build();
        }

        try {
            // Create PaymentReply object from parameters
            PaymentReply paymentReply = new PaymentReply();
            paymentReply.setCurrency(currency);
            paymentReply.setSuccess(success);
            paymentReply.setMerchantId(finalMerchantId);
            paymentReply.setOrderId(finalOrderId);
            paymentReply.setClientId(finalClientId);
            paymentReply.setAmount(parseBigDecimal(amount));
            paymentReply.setSignature(signature);
            paymentReply.setDetail(detail);

            // Log the webhook processing
            LOG.info(
                "Webhook received from IP: {} - MerchantID: {}, OrderID: {}, Success: {}, Amount: {}, Currency: {}, Detail: {}",
                remoteIp,
                finalMerchantId,
                finalOrderId,
                success,
                amount,
                currency,
                detail
            );

            // Process the webhook
            boolean processed = paymentTransactionService.processWebhook(paymentReply);

            if (processed) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            LOG.error("Error processing webhook from IP: {} - MerchantID: {}, OrderID: {}", remoteIp, finalMerchantId, finalOrderId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get the remote IP address from the request, handling proxy headers.
     *
     * @param request the HTTP request
     * @return the remote IP address
     */
    private String getRemoteIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Parse BigDecimal from string, returning null if parsing fails.
     *
     * @param value the string value to parse
     * @return the BigDecimal value or null
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
