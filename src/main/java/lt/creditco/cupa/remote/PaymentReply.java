package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PaymentReply {

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("clientID")
    private String clientId;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("date")
    private Instant date;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("merchant")
    private String merchant;

    @JsonProperty("merchantID")
    private String merchantId;

    @JsonProperty("orderID")
    private String orderId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("result")
    private String result;

    @JsonProperty("settlement")
    private LocalDate settlement;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("success")
    private String success;

    @JsonProperty("url")
    private String url;
}
