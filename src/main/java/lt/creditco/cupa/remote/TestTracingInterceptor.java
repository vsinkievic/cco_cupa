package lt.creditco.cupa.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class TestTracingInterceptor implements ClientHttpRequestInterceptor {

    private final ThreadLocal<Trace> lastTrace = new ThreadLocal<>();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Trace trace = new Trace();
        trace.setRequestBody(new String(body, StandardCharsets.UTF_8));

        ClientHttpResponse response = execution.execute(request, body);

        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBody.append(line);
            }
        }
        trace.setResponseBody(responseBody.toString());
        lastTrace.set(trace);

        return response;
    }

    public Trace getLastTrace() {
        return lastTrace.get();
    }

    public void clear() {
        lastTrace.remove();
    }

    @Getter
    public static class Trace {

        private String requestBody;
        private String responseBody;

        void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }
    }
}
