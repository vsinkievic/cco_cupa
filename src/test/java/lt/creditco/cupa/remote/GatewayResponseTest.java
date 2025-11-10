package lt.creditco.cupa.remote;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.bpmid.vapp.config.JacksonConfiguration;
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
    void queryPaymentResponseSuccessWithDateNotZoned() throws JsonProcessingException {
        String json =
            """
            {
                "response":{"statusCode":200,"message":"OK"},
                "reply":{
                    "date":"2025-08-22T06:44:01.980",
                    "reason":"Could not find this transaction",
                    "amount":"22.04",
                    "clientID":"tcln-001",
                    "orderID":"ttt-006",
                    "signature":"eae7bc256a2d279d04aa37ad5d66cf0b",
                    "merchant":"CREDITCO PROCESSING(USD)",
                    "url":"https://services.creditco.lt/?success=N&merchantID=2b47b788-d503-440d-9a93-2c9c6bea3552&orderID=ttt-006&clientID=tcln-001&detail=Transaction+abandoned&signature=eae7bc256a2d279d04aa37ad5d66cf0b",
                    "result":"11",
                    "balance":"0.00",
                    "merchantID":"2b47b788-d503-440d-9a93-2c9c6bea3552",
                    "success":"N",
                    "currency":"USD",
                    "detail":"Transaction abandoned"
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
        assertEquals(new BigDecimal("22.04"), reply.getAmount());
        assertEquals(new BigDecimal("0.00"), reply.getBalance());
        assertEquals("tcln-001", reply.getClientId());
        assertEquals("USD", reply.getCurrency());
        assertEquals(Instant.parse("2025-08-22T06:44:01.980Z"), reply.getDate());
        assertEquals("Transaction abandoned", reply.getDetail());
        assertEquals("CREDITCO PROCESSING(USD)", reply.getMerchant());
        assertEquals("2b47b788-d503-440d-9a93-2c9c6bea3552", reply.getMerchantId());
        assertEquals("ttt-006", reply.getOrderId());
        assertEquals("Could not find this transaction", reply.getReason());
        assertEquals("11", reply.getResult());
        assertNull(reply.getSettlement()); // settlement field is not present in the JSON
        assertEquals("eae7bc256a2d279d04aa37ad5d66cf0b", reply.getSignature());
        assertEquals("N", reply.getSuccess());
        assertEquals(
            "https://services.creditco.lt/?success=N&merchantID=2b47b788-d503-440d-9a93-2c9c6bea3552&orderID=ttt-006&clientID=tcln-001&detail=Transaction+abandoned&signature=eae7bc256a2d279d04aa37ad5d66cf0b",
            reply.getUrl()
        );
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

    @Test
    void placeTransactionSuccessWithHtml() throws JsonProcessingException {
        String json =
            """
            {
                "response":{"statusCode":200,"message":"OK"},
                "reply":"<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?><html class=\\"mac chrome chrome5 webkit webkit5\\"><head><base href=\\"\\/\\"><meta name=\\"viewport\\" content=\\"width=device-width, initial-scale=1\\"><meta http-equiv=\\"X-UA-Compatible\\" content=\\"IE=edge, chrome=1\\"></head><body><h1>Please click on the link within your email to continue with the deposit process.</h1><br><h1>请点击电子邮件中的链接继续存款流程.</h1></body></html>"
            }
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, PaymentReply.class);
        GatewayResponse<PaymentReply> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(200, response.getResponse().getStatusCode());
        assertEquals("OK", response.getResponse().getMessage());
        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html class=\"mac chrome chrome5 webkit webkit5\"><head><base href=\"/\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge, chrome=1\"></head><body><h1>Please click on the link within your email to continue with the deposit process.</h1><br><h1>请点击电子邮件中的链接继续存款流程.</h1></body></html>",
            response.getReply().getHtml()
        );
    }

    @Test
    void getClientResponse() throws JsonProcessingException {
        String json =
            """
            {"response":{"statusCode":200,"message":"OK"},"client":{"merchantID":"2b47b788-d503-440d-9a93-2c9c6bea3552","merchantName":"CREDITCO (USD)","merchantShortName":"CREDITCO PROCESSING(USD)","mobileNumber":"+37061495615","clientID":"CLN-001","created":"2025-08-05T15:40:22.880","updated":"2025-08-05T15:46:49.679","name":"Val Sin","id":"2bd56b54-6c72-45c2-b5a7-0ea5fc5f8ac5","emailAddress":"valdemar@sinkievic.lt","black":false,"billingAddress":{"city":"Vilnius","postCode":"LT-0000","country":"Lithuania","valid":false},"valid":true,"correlatedBlack":false}}
            """;

        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, ClientDetails.class);
        GatewayResponse<ClientDetails> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(200, response.getResponse().getStatusCode());
        assertEquals("OK", response.getResponse().getMessage());

        ClientDetails client = response.getReply();
        assertNotNull(client);
        assertEquals("2b47b788-d503-440d-9a93-2c9c6bea3552", client.getMerchantId());
        assertEquals("CREDITCO (USD)", client.getMerchantName());
        assertEquals("+37061495615", client.getMobileNumber());
        assertEquals("CLN-001", client.getClientId());
        assertEquals("2025-08-05T15:40:22.880", client.getCreatedInGateway());
        assertEquals("2025-08-05T15:46:49.679", client.getUpdatedInGateway());
        assertEquals("Val Sin", client.getName());
        assertEquals("2bd56b54-6c72-45c2-b5a7-0ea5fc5f8ac5", client.getIdInGateway());
        assertEquals("valdemar@sinkievic.lt", client.getEmailAddress());
        assertFalse(client.getBlack());
        assertTrue(client.getIsValid());
        assertFalse(client.getCorrelatedBlack());

        BillingAddress address = client.getBillingAddress();
        assertNotNull(address);
        assertEquals("Vilnius", address.getCity());
        assertEquals("LT-0000", address.getPostCode());
        assertEquals("Lithuania", address.getCountry());
        assertFalse(address.getIsValid());
    }

    @Test
    void getClientListResponse() throws JsonProcessingException {
        String json =
            """
            {
            "response":
            {
            "statusCode":200,"message":"OK"
            },
            "clients":[
            {
            "merchantID":"1234abcd-5678-efgh-9012-123456abcdef",
            "merchantName":"The Merchant",
            "mobileNumber":"222333444555",
            "clientID":"TheClient_1",
            "created":"2023-11-06T06:46:53.953",
            "updated":"2024-07-14T08:18:22.747",
            "name":"First Client",
            "id":"16639678-2c4f-481b-bda6-3144cf9ae8fb",
            "emailAddress":"client_1@client1.com",
            "black":false,
            "valid":true,
            "correlatedBlack":false
            },
            {
            "merchantID":"1234abcd-5678-efgh-9012-123456abcdef",
            "merchantName":"The Merchant",
            "mobileNumber":"666777888",
            "clientID":"TheClient_2",
            "created":"2023-11-06T08:06:26.065",
            "updated":"2024-07-14T06:51:57.540",
            "name":"Second Client",
            "id":"826f55b0-4f7d-4700-b53c-b616bceef3f6",
            "emailAddress":"client_2@client2.com",
            "black":false,
            "valid":true,
            "correlatedBlack":false
            }
            ],
            "next": "nextClientID"
            }
            """;

        JavaType innerType = TypeFactory.defaultInstance().constructCollectionType(List.class, ClientDetails.class);
        JavaType type = TypeFactory.defaultInstance().constructParametricType(GatewayResponse.class, innerType);
        GatewayResponse<List<ClientDetails>> response = objectMapper.readValue(json, type);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertEquals(200, response.getResponse().getStatusCode());
        assertEquals("OK", response.getResponse().getMessage());
        assertEquals("nextClientID", response.getNext());

        List<ClientDetails> clients = response.getReply();
        assertNotNull(clients);
        assertEquals(2, clients.size());

        ClientDetails client1 = clients.get(0);
        assertEquals("1234abcd-5678-efgh-9012-123456abcdef", client1.getMerchantId());
        assertEquals("The Merchant", client1.getMerchantName());
        assertEquals("222333444555", client1.getMobileNumber());
        assertEquals("TheClient_1", client1.getClientId());
        assertEquals("2023-11-06T06:46:53.953", client1.getCreatedInGateway());
        assertEquals("2024-07-14T08:18:22.747", client1.getUpdatedInGateway());
        assertEquals("First Client", client1.getName());
        assertEquals("16639678-2c4f-481b-bda6-3144cf9ae8fb", client1.getIdInGateway());
        assertEquals("client_1@client1.com", client1.getEmailAddress());
        assertFalse(client1.getBlack());
        assertTrue(client1.getIsValid());
        assertFalse(client1.getCorrelatedBlack());

        ClientDetails client2 = clients.get(1);
        assertEquals("1234abcd-5678-efgh-9012-123456abcdef", client2.getMerchantId());
        assertEquals("The Merchant", client2.getMerchantName());
        assertEquals("666777888", client2.getMobileNumber());
        assertEquals("TheClient_2", client2.getClientId());
        assertEquals("2023-11-06T08:06:26.065", client2.getCreatedInGateway());
        assertEquals("2024-07-14T06:51:57.540", client2.getUpdatedInGateway());
        assertEquals("Second Client", client2.getName());
        assertEquals("826f55b0-4f7d-4700-b53c-b616bceef3f6", client2.getIdInGateway());
        assertEquals("client_2@client2.com", client2.getEmailAddress());
        assertFalse(client2.getBlack());
        assertTrue(client2.getIsValid());
        assertFalse(client2.getCorrelatedBlack());
    }
}
