package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDetails {

    private String pan; // The card number, masked
    private String expiry; // The card expiry date in MM/YY format
    private String name; // The name of the cardholder
    private String expiryMonth; // The card expiry month in MM format
    private String expiryYear; // The card expiry year in YYYY format

    @JsonProperty("default")
    private Boolean isDefault; // Set to true if this is the default card for the client

    @JsonProperty("valid")
    private Boolean isValid; // Flags whether the card has been validated, set by the MYGW system

    @JsonIgnore
    private String firstErrorMessage;

    @JsonIgnore
    public boolean isValidForRequest() {
        firstErrorMessage = null;
        if (isValid != null) return error("isValid cannot be set");
        if (expiryMonth != null) return error("expiryMonth cannot be set!");
        if (expiryYear != null) return error("expiryYear cannot be set!");

        return true;
    }

    private boolean error(String message) {
        this.firstErrorMessage = message;
        return false;
    }
}
