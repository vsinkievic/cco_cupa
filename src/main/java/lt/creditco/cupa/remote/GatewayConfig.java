package lt.creditco.cupa.remote;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class GatewayConfig {

    private String baseUrl;
    private String replyUrl;
    private String backofficeUrl;
    private String merchantMid;
    private String merchantKey;
    private String apiKey;
    private String merchantCurrency;
    private String paymentType;
}
