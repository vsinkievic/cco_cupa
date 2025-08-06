package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayMessage {

    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("reason")
    private String reason;
}
