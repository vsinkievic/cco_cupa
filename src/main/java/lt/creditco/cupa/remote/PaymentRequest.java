package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequest {

    @JsonProperty("clientID")
    private String clientId;

    private ClientDetails client;
    private String signature;
    private String signatureVersion;
    private String echo;

    @JsonProperty("orderID")
    private String orderId;

    @JsonProperty("replyURL")
    private String replyUrl;

    @JsonProperty("backofficeURL")
    private String backofficeUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amount;

    private String currency;
    private CardType cardType;
    private int sendEmail;
}
