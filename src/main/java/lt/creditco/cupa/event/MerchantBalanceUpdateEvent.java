package lt.creditco.cupa.event;

import java.math.BigDecimal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MerchantBalanceUpdateEvent extends ApplicationEvent {

    private final String merchantId;
    private final BigDecimal amount;

    public MerchantBalanceUpdateEvent(Object source, String merchantId, BigDecimal amount) {
        super(source);
        this.merchantId = merchantId;
        this.amount = amount;
    }
}
