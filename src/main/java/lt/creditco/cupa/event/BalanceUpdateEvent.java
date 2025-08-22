package lt.creditco.cupa.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Application event fired when a webhook is processed but the balance is null,
 * indicating that a remote query is needed to fetch the balance.
 */
@Getter
public class BalanceUpdateEvent extends ApplicationEvent {

    private final String transactionId;
    private final String merchantId;
    private final String orderId;

    public BalanceUpdateEvent(Object source, String transactionId, String merchantId, String orderId) {
        super(source);
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.orderId = orderId;
    }
}
