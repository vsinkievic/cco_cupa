package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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

    @JsonIgnore
    private String firstErrorMessage;

    @JsonIgnore
    public boolean isValidForRequest() {
        firstErrorMessage = null;

        if (StringUtils.isBlank(clientId)) {
            if (this.client == null || StringUtils.isBlank(this.client.getClientId())) {
                return error("clientId cannot be blank");
            } else {
                this.clientId = this.client.getClientId();
            }
        }
        if (this.client != null && !StringUtils.equals(this.client.getClientId(), this.clientId)) {
            return error(String.format("clientId mismatch (%s and %s)", this.clientId, this.client.getClientId()));
        }

        if (this.client != null && !this.client.isValidForRequest()) return error(
            String.format("client is not valid (%s)", this.client.getFirstErrorMessage())
        );

        return true;
    }

    private boolean error(String message) {
        this.firstErrorMessage = message;
        return false;
    }
}
