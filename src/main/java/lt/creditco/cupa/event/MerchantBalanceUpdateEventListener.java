package lt.creditco.cupa.event;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.repository.MerchantRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantBalanceUpdateEventListener {

    private final MerchantRepository merchantRepository;

    @EventListener
    @Async
    public void handleMerchantBalanceUpdateEvent(MerchantBalanceUpdateEvent event) {
        log.info("Processing merchant balance update event for merchantId: {}, amount: {}", event.getMerchantId(), event.getAmount());

        try {
            Merchant merchant = merchantRepository.findById(event.getMerchantId()).orElse(null);
            if (merchant == null) {
                log.warn("Merchant not found for balance update - MerchantID: {}", event.getMerchantId());
                return;
            }

            BigDecimal currentBalance = merchant.getBalance();
            BigDecimal newBalance = currentBalance != null ? currentBalance.add(event.getAmount()) : event.getAmount();

            merchant.setBalance(newBalance);
            merchantRepository.save(merchant);

            log.info(
                "Successfully updated merchant balance - MerchantID: {}, old balance: {}, amount: {}, new balance: {}",
                event.getMerchantId(),
                currentBalance,
                event.getAmount(),
                newBalance
            );
        } catch (Exception e) {
            log.error("Error processing merchant balance update event for merchantId: {}", event.getMerchantId(), e);
        }
    }
}
