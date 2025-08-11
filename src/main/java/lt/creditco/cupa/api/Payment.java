package lt.creditco.cupa.api;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class Payment {

    private String orderId;
    private String clientId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt = Instant.now();
}
