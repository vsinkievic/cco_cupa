package lt.creditco.cupa.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
import org.springframework.http.ResponseEntity;
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
        summary = "Get a payment by order ID",
        description = "Get a payment by order ID",
        parameters = @Parameter(name = "orderId", description = "Order ID", example = "9ed5abf8-f37c-495d-a9cd-527f871125c1")
    )
    @GetMapping("/payments/{orderId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String orderId, Principal principal) {
        // Business context is already available from interceptor
        CupaApiContext.CupaApiContextData context = CupaApiContext.getContext();

        log.info(
            "getPayment({}), executed by {}, merchant: {}, environment: {}",
            orderId,
            principal.getName(),
            context.getMerchantId(),
            context.getEnvironment()
        );

        Optional<PaymentTransactionDTO> paymentTransaction = paymentTransactionService.findOne(orderId);
        if (paymentTransaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(paymentMapper.toPayment(paymentTransaction.get()));
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

        // Your existing business logic
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setClientId(request.getClientId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().name());
        payment.setStatus("PENDING");

        return ResponseEntity.created(new URI("/api/payments/" + payment.getOrderId())).body(payment);
    }
}
