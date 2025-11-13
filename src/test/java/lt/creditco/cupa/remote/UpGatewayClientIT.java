package lt.creditco.cupa.remote;

import static lt.creditco.cupa.remote.TestRandomData.random5DigitNumber;
import static lt.creditco.cupa.remote.TestRandomData.randomFrom;
import static lt.creditco.cupa.remote.TestRandomData.randomValueFrom;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import com.bpmid.vapp.config.JacksonConfiguration;
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
        RestTemplateBodyInterceptor.class,
        RemoteClientConfig.class,
        JacksonConfiguration.class,
        JacksonAutoConfiguration.class,
    }
)
@Disabled
@Slf4j
class UpGatewayClientIT {

    private static final String CONFIG_FILE = "src/test/resources/test_gateway_config.properties";

    @Autowired
    private UpGatewayClient upGatewayClient;

    @Autowired
    private RestTemplateBodyInterceptor restTemplateBodyInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    private GatewayConfig gatewayConfig;

    @BeforeEach
    void setUp() throws IOException {
        restTemplateBodyInterceptor.clear();
        gatewayConfig = loadGatewayConfig();
    }

    private GatewayConfig loadGatewayConfig() throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(CONFIG_FILE))) {
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
        request.setAmount(new BigDecimal(String.format("10.%d", System.currentTimeMillis() % 100)));
        request.setCurrency("USD");
        request.setClientId("CLN-001");
        request.setCardType(CardType.UnionPay);
        request.setSendEmail(1);

        // When
        GatewayResponse<PaymentReply> transactionResponse = upGatewayClient.placeTransaction(request, gatewayConfig);

        // Then
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        log.info("Transaction Response: {}", objectMapper.writeValueAsString(transactionResponse));
        assertNotNull(transactionResponse);
        assertNotNull(transactionResponse.getReply());
        assertNotNull(transactionResponse.getReply().getOrderId());
    }

    @Test
    void testPlaceTransactionWithNewClient() throws JsonProcessingException {
        log.info("-------------------------------- testPlaceTransactionWithNewClient --------------------------------");
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("test-order-" + System.currentTimeMillis());
        request.setAmount(new BigDecimal(String.format("20.%d", System.currentTimeMillis() % 100)));
        request.setCurrency("USD");
        request.setCardType(CardType.UnionPay);
        request.setSendEmail(1);

        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setClientId(String.format("CLN-%d", System.currentTimeMillis() % 1000000));
        clientDetails.setMobileNumber(String.format("+370614%d", System.currentTimeMillis() % 100000));
        clientDetails.setEmailAddress(String.format("tester_%d@test.dev", System.currentTimeMillis() % 100000));
        clientDetails.setName(String.format("Tester %d", System.currentTimeMillis() % 100000));
        request.setClient(clientDetails);

        // When
        GatewayResponse<PaymentReply> transactionResponse = upGatewayClient.placeTransaction(request, gatewayConfig);

        // Then
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        log.info("Transaction Response: {}", objectMapper.writeValueAsString(transactionResponse));
        assertNotNull(transactionResponse);
        assertNotNull(transactionResponse.getReply());
        assertNotNull(transactionResponse.getReply().getOrderId());
    }

    @Test
    void testPlaceTransactionWithNewClientWitAddress() throws JsonProcessingException {
        log.info("-------------------------------- testPlaceTransactionWithNewClientWitAddress --------------------------------");
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("test-order-" + System.currentTimeMillis());
        request.setAmount(new BigDecimal(String.format("30.%d", System.currentTimeMillis() % 100)));
        request.setCurrency("USD");
        request.setCardType(CardType.UnionPay);
        request.setSendEmail(1);

        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setClientId(String.format("CLN-%d", System.currentTimeMillis() % 1000000));
        clientDetails.setMobileNumber(String.format("+370614%d", System.currentTimeMillis() % 100000));
        clientDetails.setEmailAddress(String.format("tester_%d@test.dev", System.currentTimeMillis() % 100000));
        clientDetails.setName(
            randomValueFrom("John", "Jane", "Jim", "Jill") + " " + randomValueFrom("Doe", "Smith", "Johnson", "Williams")
        );
        request.setClient(clientDetails);

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setCity(randomValueFrom("Vilnius", "Warsaw", "Riga", "London"));
        billingAddress.setStreetName(randomValueFrom("Long Street", "Wide avenue", "Developers Street", "Test Street 4"));
        billingAddress.setStreetNumber(String.format("%d-%d", randomFrom(1, 100), randomFrom(1, 100)));
        billingAddress.setPostCode(random5DigitNumber());
        billingAddress.setCountry(randomValueFrom("LT", "PL", "LV", "GB"));
        clientDetails.setBillingAddress(billingAddress);

        // When
        GatewayResponse<PaymentReply> transactionResponse = upGatewayClient.placeTransaction(request, gatewayConfig);

        // Then
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
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
        String orderId = "ttt-007";
        //        String orderId = "408818978";
        //        String orderId = "2025080501";
        //        String orderId = "408617236";
        //String orderId = "FakeOrderId";

        // When
        GatewayResponse<PaymentReply> transactionResponse = null;
        try {
            transactionResponse = upGatewayClient.queryTransaction(orderId, gatewayConfig);
        } catch (Exception e) {
            log.error("Error querying transaction", e);
        }

        // Then
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
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
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        assertNotNull(clientDetailsResponse);
    }

    @Test
    void testGetClientList() {
        log.info("-------------------------------- testGetClientList --------------------------------");
        // Given
        String nextClientId = null;

        // When
        GatewayResponse<List<ClientDetails>> clientListResponse = null;
        try {
            clientListResponse = upGatewayClient.getClientList(nextClientId, gatewayConfig);
            log.info("Client List Response: {}", objectMapper.writeValueAsString(clientListResponse));
        } catch (Exception e) {
            log.error("Error getting client list", e);
        }

        // Then
        RestTemplateBodyInterceptor.Trace trace = restTemplateBodyInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
        assertNotNull(clientListResponse);
    }
}
