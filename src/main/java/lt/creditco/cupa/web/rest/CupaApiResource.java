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
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.api.Payment;
import lt.creditco.cupa.api.PaymentRequest;
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

    @Tag(name = "Payments")
    @Operation(
        summary = "Get a payment by order ID",
        description = "Get a payment by order ID",
        parameters = @Parameter(name = "orderId", description = "Order ID", example = "9ed5abf8-f37c-495d-a9cd-527f871125c1")
    )
    @GetMapping("/payments/{orderId}")
    public Payment getPayment(@PathVariable String orderId, Principal principal) {
        log.info("getPayment method called with orderId: {}, executed by {}", orderId, principal.getName());
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setClientId("123");
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setCurrency("EUR");
        payment.setStatus("PENDING");
        return payment;
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
        log.info("createPayment({}), executed by {}", request, principal.getName());
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setClientId(request.getClientId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().name());
        payment.setStatus("PENDING");
        return ResponseEntity.created(new URI("/api/payments/" + payment.getOrderId())).body(payment);
    }
}
