package lt.creditco.cupa.remote;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;

public class GatewayResponseDeserializer extends JsonDeserializer<GatewayResponse<?>> implements ContextualDeserializer {

    private JavaType replyType;

    public GatewayResponseDeserializer() {}

    public GatewayResponseDeserializer(JavaType replyType) {
        this.replyType = replyType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = ctxt.getContextualType();
        JavaType valueType = wrapperType.containedType(0);
        return new GatewayResponseDeserializer(valueType);
    }

    @Override
    public GatewayResponse<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        GatewayResponse<Object> response = new GatewayResponse<>();

        if (root.has("response")) {
            response.setResponse(mapper.treeToValue(root.get("response"), GatewayMessage.class));
        }

        JsonNode replyNode = null;
        if (root.has("reply")) {
            replyNode = root.get("reply");
        } else if (root.has("client")) {
            replyNode = root.get("client");
        } else if (root.has("clients")) {
            replyNode = root.get("clients");
        }

        if (replyNode != null && replyType != null) {
            Object reply;
            if (replyNode.isTextual()) {
                // Handle case where reply is a string (e.g., HTML content)
                try {
                    reply = mapper
                        .getTypeFactory()
                        .constructType(replyType.getRawClass())
                        .getRawClass()
                        .getDeclaredConstructor()
                        .newInstance();
                    // Set the html field if it exists
                    try {
                        reply.getClass().getMethod("setHtml", String.class).invoke(reply, replyNode.asText());
                    } catch (Exception e) {
                        // If setHtml method doesn't exist, try to set the field directly
                        try {
                            reply.getClass().getDeclaredField("html").setAccessible(true);
                            reply.getClass().getDeclaredField("html").set(reply, replyNode.asText());
                        } catch (Exception ex) {
                            // If html field doesn't exist, just use the string as is
                            reply = replyNode.asText();
                        }
                    }
                } catch (Exception e) {
                    // Fallback to string if object creation fails
                    reply = replyNode.asText();
                }
            } else {
                // Handle case where reply is a JSON object
                reply = mapper.convertValue(replyNode, replyType);
            }
            response.setReply(reply);
        }

        if (root.has("next")) {
            response.setNext(root.get("next").asText());
        }

        return response;
    }
}
