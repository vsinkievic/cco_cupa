package lt.creditco.cupa.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.remote.CardType;
import lt.creditco.cupa.remote.PaymentCurrency;

@Data
@Schema(description = "Payment request to initiate a new payment")
public class PaymentRequest implements MerchantOwnedEntity {

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

    @Schema(
        required = false,
        title = "Client",
        description = "Client details. Required if client is new (was not previously created).",
        example = "{\"name\": \"John Doe\", \"emailAddress\": \"john.doe@example.com\", \"mobileNumber\": \"+37061234567\"}"
    )
    private PaymentClient client;

    @Schema(required = true, title = "Amount", description = "Amount of the payment. Example: 100.00", example = "100.00")
    private BigDecimal amount;

    @Schema(required = true, title = "Currency", description = "Currency of the payment. Example: USD, EUR, etc.", example = "USD")
    private PaymentCurrency currency;

    @Schema(
        required = true,
        title = "Card type",
        description = "Card type of the payment. Example: UnionPay, WechatPay, Alipay",
        example = "UnionPay"
    )
    private CardType cardType;

    @Schema(required = true, title = "Payment flow", description = "Payment flow of the payment. Example: EMAIL, ONLINE", example = "EMAIL")
    private PaymentFlow paymentFlow;

    @Schema(
        required = false,
        title = "Merchant ID",
        description = "Specifies the Merchant ID (MID) for the payment. If omitted, the system defaults to the MID defined in the user's configuration.",
        example = "MER-00001"
    )
    private String merchantId;

    @Schema(
        required = false,
        title = "Reply URL",
        description = "The ‘front end’ URL, called by payment gateway once " +
        "the transaction completes with the result of the transaction. " +
        "This URL will be displayed to the client to inform them of the result of the transaction. " +
        "If this field is not set or left blank, the URL stored on the server for the merchant/PSP will be used.",
        example = "https://www.example.com/payment/success"
    )
    private String replyUrl;
}
