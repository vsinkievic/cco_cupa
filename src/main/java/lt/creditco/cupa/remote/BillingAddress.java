package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillingAddress {

    private String streetName;
    private String city;
    private String state;
    private String postCode;
    private String country;
}
