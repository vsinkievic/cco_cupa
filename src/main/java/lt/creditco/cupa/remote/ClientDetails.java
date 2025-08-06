package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientDetails {

    @JsonProperty("clientID")
    private String clientId; // A unique reference for the client in the merchant’s system

    @JsonProperty("merchantID")
    private String merchantId; // The merchant’s ID in the MYGW system

    private String name; // The client’s full name
    private String mobileNumber; // The client’s mobile phone number
    private String emailAddress; // The client’s email address

    private String clientPhone; // The client’s home or office phone number - additional phone number for the client

    private BillingAddress billingAddress; // The client's billing address
    private List<CardDetails> cards; // A list of the client's cards

    // data from gateway
    @JsonProperty("id")
    private String idInGateway; // The client’s unique ID in the MYGW system

    private Boolean black; // Flags whether the client has been blacklisted
    private Boolean correlatedBlack; // Flags whether any of the client's details are on the blacklist
    private String merchantName; // The merchant’s name

    @JsonProperty("created")
    private String createdInGateway; // The date and time the client was created in the MYGW system

    @JsonProperty("updated")
    private String updatedInGateway; // The date and time the client was last updated in the MYGW system

    @JsonProperty("valid")
    private Boolean isValid; // Flags whether the client has been validated, set by the MYGW system

    @JsonIgnore
    private String firstErrorMessage;

    @JsonIgnore
    public boolean isValidForRequest() {
        firstErrorMessage = null;
        if (isValid != null) return error("isValid cannot be set");
        if (black != null) return error("black cannot be set");
        if (correlatedBlack != null) return error("correlatedBlack cannot be set");
        if (createdInGateway != null) return error("createdInGateway cannot be set");
        if (updatedInGateway != null) return error("updatedInGateway cannot be set");
        if (idInGateway != null) return error("idInGateway cannot be set");

        if (StringUtils.isBlank(clientId)) return error("clientId cannot be blank");

        if (this.cards != null && this.cards.size() > 3) return error("max 3 cards allowed");

        if (this.billingAddress != null && !this.billingAddress.isValidForRequest()) return error(
            String.format("billingAddress is not valid (%s)", this.billingAddress.getFirstErrorMessage())
        );

        return true;
    }

    private boolean error(String message) {
        this.firstErrorMessage = message;
        return false;
    }
}
