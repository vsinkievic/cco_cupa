package lt.creditco.cupa.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentRequest;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.service.mapper.PaymentMapper;
import lt.creditco.cupa.web.context.CupaApiContext;
import lt.creditco.cupa.web.rest.util.AccessControlHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class CupaApiResource {

    private final PaymentTransactionService paymentTransactionService;
    private final PaymentMapper paymentMapper;

    public CupaApiResource(PaymentTransactionService paymentTransactionService, PaymentMapper paymentMapper) {
        this.paymentTransactionService = paymentTransactionService;
        this.paymentMapper = paymentMapper;
    }

    @Tag(name = "Payments")
    @Operation(
        summary = "Get a payment by order ID for a specific merchant",
        description = "Get a payment by order ID for a specific merchant. User must have access to the specified merchant.",
        parameters = {
            @Parameter(name = "merchantId", description = "Merchant ID", example = "MER-00001"),
            @Parameter(name = "orderId", description = "Order ID", example = "9ed5abf8-f37c-495d-a9cd-527f871125c1"),
        }
    )
    @GetMapping("/merchants/{merchantId}/payments/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderIdForMerchant(
        @PathVariable String merchantId,
        @PathVariable String orderId,
        Principal principal
    ) {
        // Business context is already available from interceptor
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();

        log.info(
            "getPaymentByOrderIdForMerchant({}, {}), executed by {}, merchant: {}, environment: {}",
            merchantId,
            orderId,
            principal.getName(),
            context.getMerchantId(),
            context.getEnvironment()
        );

        // Check if user can access the specified merchant
        if (context.getCupaUser() != null && !context.getCupaUser().getMerchantIdsSet().contains(merchantId)) {
            throw new AccessDeniedException(String.format("Access denied for merchant: %s", merchantId));
        }

        Optional<PaymentTransactionDTO> paymentTransaction = paymentTransactionService.findByMerchantIdAndOrderId(merchantId, orderId);
        return AccessControlHelper.checkAccessAndReturn(paymentTransaction, context, paymentMapper::toPayment);
    }

    @Tag(name = "Payments")
    @Operation(
        summary = "Get a payment by ID",
        description = "Get a payment by its internal ID. User must have access to the payment's merchant.",
        parameters = @Parameter(name = "id", description = "Payment ID", example = "9ed5abf8-f37c-495d-a9cd-527f871125c1")
    )
    @GetMapping("/payments/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id, Principal principal) {
        // Business context is already available from interceptor
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();

        log.info(
            "getPaymentById({}), executed by {}, merchant: {}, environment: {}",
            id,
            principal.getName(),
            context.getMerchantId(),
            context.getEnvironment()
        );

        Optional<PaymentTransactionDTO> paymentTransaction = paymentTransactionService.findOne(id);
        return AccessControlHelper.checkAccessAndReturn(paymentTransaction, context, paymentMapper::toPayment);
    }

    @Tag(name = "Payments")
    @Operation(
        summary = "Initiate a new payment",
        description = "<p>Initiate a new payment.</p><p>This endpoint can be used with a new or existing client. If client is not found, it will be created.</p>",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = PaymentRequest.class))
        )
    )
    @PostMapping("/payments")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest request, Principal principal)
        throws URISyntaxException {
        // Business context is already available from interceptor
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();

        log.info(
            "createPayment({}), executed by {}, merchant: {}, environment: {}",
            request.getOrderId(),
            principal.getName(),
            context.getMerchantId(),
            context.getEnvironment()
        );

        if (request.getMerchantId() != null && !context.canAccessEntity(request)) {
            throw new AccessDeniedException(String.format("Access denied for merchant: %s", request.getMerchantId()));
        }

        Payment payment = paymentTransactionService.createPayment(request, context);

        return ResponseEntity.created(new URI("/api/v1/payments/" + payment.getId())).body(payment);
    }
}
