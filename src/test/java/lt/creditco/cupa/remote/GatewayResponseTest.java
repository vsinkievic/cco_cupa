package lt.creditco.cupa.remote;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lt.creditco.cupa.config.JacksonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class GatewayResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            JacksonConfiguration.class,
            JacksonAutoConfiguration.class
        );
        objectMapper = context.getBean(ObjectMapper.class);
    }

    @Test
    void queryPaymentResponseSuccess() throws JsonProcessingException {
        String json =
            """
            {
            "response":{
            "statusCode":200,
            "message":"OK"
            },
            "reply":
            {
            "amount":"25.00",
            "balance":"25.00",
            "clientID":"TheClient",
            "currency":"USD",
            "date":"2024-07-16T06:20:53.281Z",
            "detail":"Successfully completed",
            "merchant":"The Merchant",
            "merchantID":"5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e",
            "orderID":"110836419",
            "reason":"Success!",
            "result":"0",
            "settlement":"2024-07-23",
            "signature":"3a53e1e7251b08036cc2f9b8de9d2030",
            "success":"Y",
            "url":"https://www.my-gateway.net/ReturnURL.html?currency=AUD&success=Y&merchantID=5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e&orderID=110836419&clientID=NewClient&amount=25.00&signature=3a53e1e7251b08036cc2f9b8de9d2030"
            }
            }
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, PaymentReply.class);
        GatewayResponse<PaymentReply> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(200, response.getResponse().getStatusCode());
        assertEquals("OK", response.getResponse().getMessage());

        PaymentReply reply = response.getReply();
        assertNotNull(reply);
        assertEquals(new BigDecimal("25.00"), reply.getAmount());
        assertEquals(new BigDecimal("25.00"), reply.getBalance());
        assertEquals("TheClient", reply.getClientId());
        assertEquals("USD", reply.getCurrency());
        assertEquals(Instant.parse("2024-07-16T06:20:53.281Z"), reply.getDate());
        assertEquals("Successfully completed", reply.getDetail());
        assertEquals("The Merchant", reply.getMerchant());
        assertEquals("5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e", reply.getMerchantId());
        assertEquals("110836419", reply.getOrderId());
        assertEquals("Success!", reply.getReason());
        assertEquals("0", reply.getResult());
        assertEquals(LocalDate.of(2024, 7, 23), reply.getSettlement());
        assertEquals("3a53e1e7251b08036cc2f9b8de9d2030", reply.getSignature());
        assertEquals("Y", reply.getSuccess());
        assertNotNull(reply.getUrl());
    }

    @Test
    void queryPaymentResponseNotFound() throws JsonProcessingException {
        String json =
            """
            {
            "response":
            {
            "statusCode": 404,
            "message": "Not Found",
            "detail": "No OrderID found with MerchantID: 5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e"
            }
            }
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, PaymentReply.class);
        GatewayResponse<PaymentReply> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(404, response.getResponse().getStatusCode());
        assertEquals("Not Found", response.getResponse().getMessage());
        assertEquals("No OrderID found with MerchantID: 5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e", response.getResponse().getDetail());
        assertNull(response.getReply());
    }

    @Test
    void queryPaymentResponseFakeorderid() throws JsonProcessingException {
        String json =
            """
            {"response":{"statusCode":200,"message":"OK"},"reply":{"result":"11","merchantID":"2b47b788-d503-440d-9a93-2c9c6bea3552","orderID":"fakeorderid","success":"N","merchant":"CREDITCO PROCESSING(USD)","detail":"Transaction abandoned"}}
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, PaymentReply.class);
        GatewayResponse<PaymentReply> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(200, response.getResponse().getStatusCode());
        assertEquals("OK", response.getResponse().getMessage());

        PaymentReply reply = response.getReply();
        assertNotNull(reply);
        assertEquals("11", reply.getResult());
        assertEquals("2b47b788-d503-440d-9a93-2c9c6bea3552", reply.getMerchantId());
        assertEquals("fakeorderid", reply.getOrderId());
        assertEquals("N", reply.getSuccess());
        assertEquals("CREDITCO PROCESSING(USD)", reply.getMerchant());
        assertEquals("Transaction abandoned", reply.getDetail());
    }

    @Test
    void placeTransactionErrorAmountOutOfRange() throws JsonProcessingException {
        String json =
            """
            {"response":{"statusCode":400,"message":"Bad Request","detail":"Amount out of range - for this merchant; USD [min = 10.00, max = 1000.00]"}}
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, PaymentReply.class);
        GatewayResponse<PaymentReply> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(400, response.getResponse().getStatusCode());
        assertEquals("Bad Request", response.getResponse().getMessage());
        assertEquals("Amount out of range - for this merchant; USD [min = 10.00, max = 1000.00]", response.getResponse().getDetail());
        assertNull(response.getReply());
    }
}
