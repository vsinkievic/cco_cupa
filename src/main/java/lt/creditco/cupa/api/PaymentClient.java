package lt.creditco.cupa.api;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentClient {

    private String name;

    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private String emailAddress;

    private String mobileNumber;
}
