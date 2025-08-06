package lt.creditco.cupa.remote;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
@JsonDeserialize(using = GatewayResponseDeserializer.class)
public class GatewayResponse<T> {

    @JsonProperty("response")
    private GatewayMessage response;

    private T reply;

    @JsonProperty("next")
    private String next;

    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        if ("reply".equals(name) || "client".equals(name) || "clients".equals(name)) {
            // This will be handled by a custom deserializer
        } else {
            additionalProperties.put(name, value);
        }
    }
}
