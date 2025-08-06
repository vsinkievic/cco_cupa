package lt.creditco.cupa.remote;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.config.RemoteClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { UpGatewayClient.class, TestTracingInterceptor.class, RemoteClientConfig.class })
// @Disabled
@Slf4j
class UpGatewayClientIT {

    @Autowired
    private UpGatewayClient upGatewayClient;

    @Autowired
    private TestTracingInterceptor testTracingInterceptor;

    @BeforeEach
    void setUp() {
        testTracingInterceptor.clear();
    }

    @Test
    void testPlaceTransaction() {
        // Given
        GatewayConfig config = GatewayConfig.builder()
            .baseUrl("https://api.test.oneroadpayments.com")
            .replyUrl("https://servicec.creditco.lt:8443/cupa/reply")
            .backofficeUrl("https://servicec.creditco.lt:8443/cupa/backoffice")
            .merchantMid("2b47b788-d503-440d-9a93-2c9c6bea3552")
            .merchantKey("3631961956")
            .apiKey("YYNNLZPh7J42qdgDaMJP6TvkP7NAtMA4xwIGMAv7")
            .merchantCurrency("USD")
            //            .paymentType("UnionPay")
            .build();

        PaymentRequest request = new PaymentRequest();
        request.setOrderID("test-order-" + System.currentTimeMillis());
        request.setAmount(new BigDecimal("10.12"));
        request.setCurrency("USD");
        request.setClientID("CLN-001");
        request.setCardType(CardType.UnionPay);

        // When
        String actualResponse = upGatewayClient.placeTransaction(request, config);

        // Then
        assertNotNull(actualResponse);
        TestTracingInterceptor.Trace trace = testTracingInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
    }

    @Test
    void testQueryTransaction() {
        // Given
        GatewayConfig config = GatewayConfig.builder()
            .baseUrl("https://api.test.oneroadpayments.com")
            .replyUrl("https://servicec.creditco.lt:8443/cupa/reply")
            .backofficeUrl("https://servicec.creditco.lt:8443/cupa/backoffice")
            .merchantMid("2b47b788-d503-440d-9a93-2c9c6bea3552")
            .merchantKey("3631961956")
            .apiKey("YYNNLZPh7J42qdgDaMJP6TvkP7NAtMA4xwIGMAv7")
            .merchantCurrency("USD")
            //           .paymentType("UnionPay")
            .build();
        //        String orderId = "test-order-1754460577204";
        //        String orderId = "408818978";
        //        String orderId = "2025080501";
        //        String orderId = "408617236";
        String orderId = "FakeOrderId";

        // When
        QueryResponse actualResponse = upGatewayClient.queryTransaction(orderId, config);

        // Then
        log.info("Response: {}", actualResponse);
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getReply());

        TestTracingInterceptor.Trace trace = testTracingInterceptor.getLastTrace();
        log.info("Request Body: {}", trace.getRequestBody());
        log.info("Response Body: {}", trace.getResponseBody());
    }
}
