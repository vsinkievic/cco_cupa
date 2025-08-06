package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientDetails {

    @JsonProperty("clientID")
    private String clientID;

    private String mobileNumber;
    private String name;
    private String emailAddress;
    private BillingAddress billingAddress;
}
