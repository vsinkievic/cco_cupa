package lt.creditco.cupa.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lt.creditco.cupa.remote.PaymentReply;
import lt.creditco.cupa.remote.SignatureVerifier;
import lt.creditco.cupa.service.PaymentTransactionService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@Disabled
class WebhookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PaymentTransactionService paymentTransactionService;

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldProcessWebhookWithAllRequiredFields() throws Exception {
        // Mock signature verification to return true
        try (MockedStatic<SignatureVerifier> mockedSignatureVerifier = Mockito.mockStatic(SignatureVerifier.class)) {
            mockedSignatureVerifier
                .when(() -> SignatureVerifier.verifyWebhookSignature(any(PaymentReply.class), anyString()))
                .thenReturn(true);

            // Mock service to return true
            when(paymentTransactionService.processWebhook(any(PaymentReply.class))).thenReturn(true);

            // Test GET request with all required fields (successful transaction)
            mvc
                .perform(
                    get("/public/webhook")
                        .param("currency", "AUD")
                        .param("success", "Y")
                        .param("merchantID", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                        .param("orderID", "110836419")
                        .param("clientID", "NewClient")
                        .param("amount", "25.00")
                        .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
                )
                .andExpect(status().isOk());

            // Capture the PaymentReply argument and verify all values
            ArgumentCaptor<PaymentReply> paymentReplyCaptor = ArgumentCaptor.forClass(PaymentReply.class);
            verify(paymentTransactionService).processWebhook(paymentReplyCaptor.capture());

            PaymentReply capturedPaymentReply = paymentReplyCaptor.getValue();
            assert capturedPaymentReply.getCurrency().equals("AUD");
            assert capturedPaymentReply.getSuccess().equals("Y");
            assert capturedPaymentReply.getMerchantId().equals("5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e");
            assert capturedPaymentReply.getOrderId().equals("110836419");
            assert capturedPaymentReply.getClientId().equals("NewClient");
            assert capturedPaymentReply.getAmount().toString().equals("25.00");
            assert capturedPaymentReply.getSignature().equals("3a53e1e7251b08036cc2f9b8de9d2030");
            assert capturedPaymentReply.getDetail() == null;
        }
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldProcessFailedTransaction() throws Exception {
        // Mock signature verification to return true
        try (MockedStatic<SignatureVerifier> mockedSignatureVerifier = Mockito.mockStatic(SignatureVerifier.class)) {
            mockedSignatureVerifier
                .when(() -> SignatureVerifier.verifyWebhookSignature(any(PaymentReply.class), anyString()))
                .thenReturn(true);

            // Mock service to return true
            when(paymentTransactionService.processWebhook(any(PaymentReply.class))).thenReturn(true);

            // Test GET request for failed transaction
            mvc
                .perform(
                    get("/public/webhook")
                        .param("success", "N")
                        .param("merchantID", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                        .param("orderID", "110836419")
                        .param("clientID", "NewClient")
                        .param("detail", "Transaction abandoned")
                        .param("signature", "1ef02c01f953aec4163bc06d4c287c6f")
                )
                .andExpect(status().isOk());

            // Capture the PaymentReply argument and verify all values
            ArgumentCaptor<PaymentReply> paymentReplyCaptor = ArgumentCaptor.forClass(PaymentReply.class);
            verify(paymentTransactionService).processWebhook(paymentReplyCaptor.capture());

            PaymentReply capturedPaymentReply = paymentReplyCaptor.getValue();
            assert capturedPaymentReply.getCurrency() == null;
            assert capturedPaymentReply.getSuccess().equals("N");
            assert capturedPaymentReply.getMerchantId().equals("5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e");
            assert capturedPaymentReply.getOrderId().equals("110836419");
            assert capturedPaymentReply.getClientId().equals("NewClient");
            assert capturedPaymentReply.getAmount() == null;
            assert capturedPaymentReply.getSignature().equals("1ef02c01f953aec4163bc06d4c287c6f");
            assert capturedPaymentReply.getDetail().equals("Transaction abandoned");
        }
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldHandleCamelCaseParameters() throws Exception {
        // Mock signature verification to return true
        try (MockedStatic<SignatureVerifier> mockedSignatureVerifier = Mockito.mockStatic(SignatureVerifier.class)) {
            mockedSignatureVerifier
                .when(() -> SignatureVerifier.verifyWebhookSignature(any(PaymentReply.class), anyString()))
                .thenReturn(true);

            // Mock service to return true
            when(paymentTransactionService.processWebhook(any(PaymentReply.class))).thenReturn(true);

            // Test with camelCase parameters (merchantId, orderId, clientId)
            mvc
                .perform(
                    get("/public/webhook")
                        .param("currency", "AUD")
                        .param("success", "Y")
                        .param("merchantId", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                        .param("orderId", "110836419")
                        .param("clientId", "NewClient")
                        .param("amount", "25.00")
                        .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
                )
                .andExpect(status().isOk());

            // Capture the PaymentReply argument and verify all values
            ArgumentCaptor<PaymentReply> paymentReplyCaptor = ArgumentCaptor.forClass(PaymentReply.class);
            verify(paymentTransactionService).processWebhook(paymentReplyCaptor.capture());

            PaymentReply capturedPaymentReply = paymentReplyCaptor.getValue();
            assert capturedPaymentReply.getCurrency().equals("AUD");
            assert capturedPaymentReply.getSuccess().equals("Y");
            assert capturedPaymentReply.getMerchantId().equals("5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e");
            assert capturedPaymentReply.getOrderId().equals("110836419");
            assert capturedPaymentReply.getClientId().equals("NewClient");
            assert capturedPaymentReply.getAmount().toString().equals("25.00");
            assert capturedPaymentReply.getSignature().equals("3a53e1e7251b08036cc2f9b8de9d2030");
            assert capturedPaymentReply.getDetail() == null;
        }
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldPreferOriginalCaseWhenBothProvided() throws Exception {
        // Mock signature verification to return true
        try (MockedStatic<SignatureVerifier> mockedSignatureVerifier = Mockito.mockStatic(SignatureVerifier.class)) {
            mockedSignatureVerifier
                .when(() -> SignatureVerifier.verifyWebhookSignature(any(PaymentReply.class), anyString()))
                .thenReturn(true);

            // Mock service to return true
            when(paymentTransactionService.processWebhook(any(PaymentReply.class))).thenReturn(true);

            // Test with both original case and camelCase parameters - should prefer original case
            mvc
                .perform(
                    get("/public/webhook")
                        .param("currency", "AUD")
                        .param("success", "Y")
                        .param("merchantID", "original-merchant-id")
                        .param("merchantId", "camel-case-merchant-id")
                        .param("orderID", "original-order-id")
                        .param("orderId", "camel-case-order-id")
                        .param("clientID", "original-client-id")
                        .param("clientId", "camel-case-client-id")
                        .param("amount", "25.00")
                        .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
                )
                .andExpect(status().isOk());

            // Capture the PaymentReply argument and verify all values
            ArgumentCaptor<PaymentReply> paymentReplyCaptor = ArgumentCaptor.forClass(PaymentReply.class);
            verify(paymentTransactionService).processWebhook(paymentReplyCaptor.capture());

            PaymentReply capturedPaymentReply = paymentReplyCaptor.getValue();
            assert capturedPaymentReply.getCurrency().equals("AUD");
            assert capturedPaymentReply.getSuccess().equals("Y");
            assert capturedPaymentReply.getMerchantId().equals("original-merchant-id");
            assert capturedPaymentReply.getOrderId().equals("original-order-id");
            assert capturedPaymentReply.getClientId().equals("original-client-id");
            assert capturedPaymentReply.getAmount().toString().equals("25.00");
            assert capturedPaymentReply.getSignature().equals("3a53e1e7251b08036cc2f9b8de9d2030");
            assert capturedPaymentReply.getDetail() == null;
        }
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldSilentlyExitWhenMerchantIdMissing() throws Exception {
        // Missing merchantID - should exit silently with 200 OK
        mvc
            .perform(
                get("/public/webhook")
                    .param("currency", "AUD")
                    .param("success", "Y")
                    .param("orderID", "110836419")
                    .param("clientID", "NewClient")
                    .param("amount", "25.00")
                    .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
            )
            .andExpect(status().isOk());

        // Verify that processWebhook was NOT called
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldSilentlyExitWhenOrderIdMissing() throws Exception {
        // Missing orderID - should exit silently with 200 OK
        mvc
            .perform(
                get("/public/webhook")
                    .param("currency", "AUD")
                    .param("success", "Y")
                    .param("merchantID", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                    .param("clientID", "NewClient")
                    .param("amount", "25.00")
                    .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
            )
            .andExpect(status().isOk());

        // Verify that processWebhook was NOT called
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldSilentlyExitWhenSignatureMissing() throws Exception {
        // Missing signature - should exit silently with 200 OK
        mvc
            .perform(
                get("/public/webhook")
                    .param("currency", "AUD")
                    .param("success", "Y")
                    .param("merchantID", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                    .param("orderID", "110836419")
                    .param("clientID", "NewClient")
                    .param("amount", "25.00")
            )
            .andExpect(status().isOk());

        // Verify that processWebhook was NOT called
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    @EnabledIf(
        expression = "#{!'prod'.equals(systemProperties['spring.profiles.active'])}",
        reason = "This test requires a mocked environment and is disabled for the 'prod' profile."
    )
    void shouldSilentlyExitWhenAllRequiredFieldsMissing() throws Exception {
        // Missing all required fields - should exit silently with 200 OK
        mvc
            .perform(
                get("/public/webhook")
                    .param("currency", "AUD")
                    .param("success", "Y")
                    .param("clientID", "NewClient")
                    .param("amount", "25.00")
            )
            .andExpect(status().isOk());

        // Verify that processWebhook was NOT called
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    void shouldHandleEmptyRequest() throws Exception {
        // Empty request - should exit silently with 200 OK
        mvc.perform(get("/public/webhook")).andExpect(status().isOk());

        // Verify that processWebhook was NOT called
        verifyNoInteractions(paymentTransactionService);
    }

    @Test
    void shouldHandleInvalidAmountFormat() throws Exception {
        // Mock signature verification to return true
        try (MockedStatic<SignatureVerifier> mockedSignatureVerifier = Mockito.mockStatic(SignatureVerifier.class)) {
            mockedSignatureVerifier
                .when(() -> SignatureVerifier.verifyWebhookSignature(any(PaymentReply.class), anyString()))
                .thenReturn(true);

            // Mock service to return true
            when(paymentTransactionService.processWebhook(any(PaymentReply.class))).thenReturn(true);

            // Invalid amount format - should still process but with null amount
            mvc
                .perform(
                    get("/public/webhook")
                        .param("currency", "AUD")
                        .param("success", "Y")
                        .param("merchantID", "5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e")
                        .param("orderID", "110836419")
                        .param("clientID", "NewClient")
                        .param("amount", "invalid-amount")
                        .param("signature", "3a53e1e7251b08036cc2f9b8de9d2030")
                )
                .andExpect(status().isOk());

            // Capture the PaymentReply argument and verify all values
            ArgumentCaptor<PaymentReply> paymentReplyCaptor = ArgumentCaptor.forClass(PaymentReply.class);
            verify(paymentTransactionService).processWebhook(paymentReplyCaptor.capture());

            PaymentReply capturedPaymentReply = paymentReplyCaptor.getValue();
            assert capturedPaymentReply.getCurrency().equals("AUD");
            assert capturedPaymentReply.getSuccess().equals("Y");
            assert capturedPaymentReply.getMerchantId().equals("5adeaafb-1b6d-4bb2-ba11-1cce35e6b38e");
            assert capturedPaymentReply.getOrderId().equals("110836419");
            assert capturedPaymentReply.getClientId().equals("NewClient");
            assert capturedPaymentReply.getAmount() == null; // Invalid amount should be null
            assert capturedPaymentReply.getSignature().equals("3a53e1e7251b08036cc2f9b8de9d2030");
            assert capturedPaymentReply.getDetail() == null;
        }
    }
}
