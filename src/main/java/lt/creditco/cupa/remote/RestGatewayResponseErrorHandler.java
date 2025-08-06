package lt.creditco.cupa.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
/* @Component
@Slf4j
public class RestGatewayResponseErrorHandler implements ResponseErrorHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        log.error("Response error: {} {}", response.getStatusCode(), response.getStatusText());
        try {
            GatewayErrorResponse errorResponse = objectMapper.readValue(response.getBody(), GatewayErrorResponse.class);
            // Here you can throw a custom exception with the parsed error details
            // For example:
            // throw new GatewayException(errorResponse);
        } catch (Exception e) {
            log.error("Error parsing error response", e);
            // Throw a generic exception if parsing fails
            // throw new GatewayException("Failed to parse error response from gateway");
        }
    }
}

*/
