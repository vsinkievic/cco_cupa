package lt.creditco.cupa.remote;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.config.JacksonConfiguration;
import lt.creditco.cupa.config.RemoteClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
        UpGatewayClient.class,
        TestTracingInterceptor.class,
        RemoteClientConfig.class,
        JacksonConfiguration.class,
        JacksonAutoConfiguration.class,
    }
)
// @Disabled
@Slf4j
class UpGatewayClientIT {

    private static final String CONFIG_FILE = "src/test/resources/test_gateway_config.properties";

    @Autowired
    private UpGatewayClient upGatewayClient;

    @Autowired
    private TestTracingInterceptor testTracingInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private GatewayConfig gatewayConfig;

    @BeforeEach
    void setUp() throws IOException {
        testTracingInterceptor.clear();
        gatewayConfig = loadGatewayConfig();
    }

    private GatewayConfig loadGatewayConfig() throws IOException {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load test gateway config file: " + CONFIG_FILE, e);
        }

        return GatewayConfig.builder()
            .baseUrl(properties.getProperty("BASE_URL"))
            .replyUrl(properties.getProperty("REPLY_URL"))
            .backofficeUrl(properties.getProperty("BACKOFFICE_URL"))
            .merchantMid(properties.getProperty("MERCHANT_MID"))
            .merchantKey(properties.getProperty("MERCHANT_KEY"))
            .apiKey(properties.getProperty("API_KEY"))
            .merchantCurrency(properties.getProperty("MERCHANT_CURRENCY"))
            .build();
    }

    @Test
    void testPlaceTransaction() throws JsonProcessingException {
        log.info("-------------------------------- testPlaceTransaction --------------------------------");
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("test-order-" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("10.12"));
        request.setCurrency("USD");
        request.setClientId("CLN-001");
        request.setCardType(CardType.UnionPay);
        request.setSendEmail(1);

        // When
        GatewayResponse<PaymentReply> transactionResponse = upGatewayClient.placeTransaction(request, gatewayConfig);

        // Then
        TestTracingInterceptor.Trace trace = testTracingInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        log.info("Transaction Response: {}", objectMapper.writeValueAsString(transactionResponse));
        assertNotNull(transactionResponse);
        assertNotNull(transactionResponse.getReply());
        assertNotNull(transactionResponse.getReply().getOrderId());
    }

    @Test
    void testQueryTransaction() throws JsonProcessingException {
        log.info("-------------------------------- testQueryTransaction --------------------------------");
        // Given
        String orderId = "test-order-1754460577204";
        //        String orderId = "408818978";
        //        String orderId = "2025080501";
        //        String orderId = "408617236";
        //String orderId = "FakeOrderId";

        // When
        GatewayResponse<PaymentReply> transactionResponse = upGatewayClient.queryTransaction(orderId, gatewayConfig);

        // Then
        TestTracingInterceptor.Trace trace = testTracingInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        log.info("Transaction Response: {}", objectMapper.writeValueAsString(transactionResponse));
        assertNotNull(transactionResponse);
        assertNotNull(transactionResponse.getReply());
    }

    @Test
    void testGetClientDetails() {
        log.info("-------------------------------- testGetClientDetails --------------------------------");
        // Given
        String clientId = "CLN-001";

        // When
        GatewayResponse<ClientDetails> clientDetailsResponse = null;
        try {
            clientDetailsResponse = upGatewayClient.getClientDetails(clientId, gatewayConfig);
            log.info("Client Details Response: {}", objectMapper.writeValueAsString(clientDetailsResponse));
        } catch (Exception e) {
            log.error("Error getting client details", e);
        }

        // Then
        TestTracingInterceptor.Trace trace = testTracingInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        assertNotNull(clientDetailsResponse);
    }
}
