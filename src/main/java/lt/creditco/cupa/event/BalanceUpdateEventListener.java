package lt.creditco.cupa.event;

import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.domain.PaymentTransaction;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.repository.PaymentTransactionRepository;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for balance update events.
 * Handles asynchronous balance updates by querying the remote gateway
 * when a webhook is processed but the balance is null.
 */
@Component
public class BalanceUpdateEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceUpdateEventListener.class);

    private final PaymentTransactionService paymentTransactionService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MerchantRepository merchantRepository;

    public BalanceUpdateEventListener(
        PaymentTransactionService paymentTransactionService,
        PaymentTransactionRepository paymentTransactionRepository,
        MerchantRepository merchantRepository
    ) {
        this.paymentTransactionService = paymentTransactionService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.merchantRepository = merchantRepository;
    }

    /**
     * Handle balance update events asynchronously.
     * This method runs outside the webhook processing transaction.
     *
     * @param event the balance update event
     */
    @EventListener
    @Async
    public void handleBalanceUpdateEvent(BalanceUpdateEvent event) {
        LOG.info(
            "Processing balance update event for transaction: {}, merchant: {}, order: {}",
            event.getTransactionId(),
            event.getMerchantId(),
            event.getOrderId()
        );

        try {
            // Find the payment transaction
            PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(event.getTransactionId()).orElse(null);
            if (paymentTransaction == null) {
                LOG.warn("Payment transaction not found for balance update: {}", event.getTransactionId());
                return;
            }

            // Find the merchant
            Merchant merchant = merchantRepository.findById(event.getMerchantId()).orElse(null);
            if (merchant == null) {
                LOG.warn("Merchant not found for balance update: {}", event.getMerchantId());
                return;
            }

            // Create a context for the merchant
            CupaApiContext.MerchantContext merchantContext = CupaApiContext.MerchantContext.builder()
                .merchantId(merchant.getId())
                .environment(merchant.getMode())
                .cupaApiKey(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getCupaProdApiKey()
                        : merchant.getCupaTestApiKey()
                )
                .mode(merchant.getMode())
                .status(merchant.getStatus())
                .gatewayUrl(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getRemoteProdUrl()
                        : merchant.getRemoteTestUrl()
                )
                .gatewayMerchantId(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getRemoteProdMerchantId()
                        : merchant.getRemoteTestMerchantId()
                )
                .gatewayMerchantKey(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getRemoteProdMerchantKey()
                        : merchant.getRemoteTestMerchantKey()
                )
                .gatewayApiKey(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getRemoteProdApiKey()
                        : merchant.getRemoteTestApiKey()
                )
                .build();

            CupaApiContext.CupaApiContextData context = CupaApiContext.CupaApiContextData.builder()
                .merchantId(merchant.getId())
                .cupaApiKey(
                    merchant.getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE
                        ? merchant.getCupaProdApiKey()
                        : merchant.getCupaTestApiKey()
                )
                .merchantContext(merchantContext)
                .build();

            // Query the payment from gateway to get the balance
            LOG.info("Querying payment from gateway for balance update: {}", event.getTransactionId());
            paymentTransactionService.queryPaymentFromGateway(event.getTransactionId(), context);

            LOG.info("Successfully processed balance update for transaction: {}", event.getTransactionId());
        } catch (Exception e) {
            LOG.error("Error processing balance update event for transaction: {}", event.getTransactionId(), e);
        }
    }
}
