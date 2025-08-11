package lt.creditco.cupa.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Schema(description = "Payment request to initiate a new payment")
public class PaymentRequest {

    @Schema(
        required = true,
        title = "Payment ID",
        description = "UUID or any string up to 50 chars length. Assigned by your system.",
        example = "9ed5abf8-f37c-495d-a9cd-527f871125c1"
    )
    private String orderId;

    @Schema(
        required = true,
        title = "Client ID",
        description = "UUID or any string up to 50 chars length. Assigned by your system.",
        example = "CLN-00001"
    )
    private String clientId;

    @Schema(required = true, title = "Amount", description = "Amount of the payment. Example: 100.00", example = "100.00")
    private BigDecimal amount;

    @Schema(required = true, title = "Currency", description = "Currency of the payment. Example: USD, EUR, etc.", example = "USD")
    private String currency;
}
