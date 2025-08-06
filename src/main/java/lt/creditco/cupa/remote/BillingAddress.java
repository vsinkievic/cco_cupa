package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingAddress {

    private String streetName; // The number on a street of the client’s postal address
    private String streetNumber; // The name of the street of the client’s postal address
    private String streetSuffix; // The tail of the street address, of the client’s postal address, typically St, Rd, etc
    private String city; // The town, city or village that is used in the client’s postal address
    private String state; // The state or region used in the client’s postal address
    private String postCode; // The postal code used in the client’s postal address
    private String country; // The country used in the client’s postal address

    @JsonProperty("valid")
    private Boolean isValid; // Flags whether the client address has been validated, set by the MYGW system

    @JsonIgnore
    private String firstErrorMessage;

    public boolean isValidForRequest() {
        firstErrorMessage = null;
        if (isValid != null) return error("isValid cannot be set");

        return true;
    }

    private boolean error(String message) {
        this.firstErrorMessage = message;
        return false;
    }
}
