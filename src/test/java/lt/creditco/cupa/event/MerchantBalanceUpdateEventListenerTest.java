package lt.creditco.cupa.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.repository.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantBalanceUpdateEventListenerTest {

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantBalanceUpdateEventListener eventListener;

    private Merchant testMerchant;
    private MerchantBalanceUpdateEvent testEvent;

    @BeforeEach
    void setUp() {
        testMerchant = new Merchant();
        testMerchant.setId("test-merchant-id");
        testMerchant.setBalance(new BigDecimal("100.00"));

        testEvent = new MerchantBalanceUpdateEvent(this, "test-merchant-id", new BigDecimal("50.00"));
    }

    @Test
    void shouldUpdateMerchantBalanceSuccessfully() {
        // Given
        when(merchantRepository.findById("test-merchant-id")).thenReturn(Optional.of(testMerchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(testMerchant);

        // When
        eventListener.handleMerchantBalanceUpdateEvent(testEvent);

        // Then
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantRepository).save(merchantCaptor.capture());

        Merchant savedMerchant = merchantCaptor.getValue();
        assertThat(savedMerchant.getBalance()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldSetBalanceWhenCurrentBalanceIsNull() {
        // Given
        testMerchant.setBalance(null);
        when(merchantRepository.findById("test-merchant-id")).thenReturn(Optional.of(testMerchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(testMerchant);

        // When
        eventListener.handleMerchantBalanceUpdateEvent(testEvent);

        // Then
        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        verify(merchantRepository).save(merchantCaptor.capture());

        Merchant savedMerchant = merchantCaptor.getValue();
        assertThat(savedMerchant.getBalance()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldHandleMerchantNotFound() {
        // Given
        when(merchantRepository.findById("non-existent-merchant")).thenReturn(Optional.empty());

        MerchantBalanceUpdateEvent event = new MerchantBalanceUpdateEvent(this, "non-existent-merchant", new BigDecimal("50.00"));

        // When
        eventListener.handleMerchantBalanceUpdateEvent(event);

        // Then
        verify(merchantRepository, never()).save(any(Merchant.class));
    }

    @Test
    void shouldHandleRepositoryException() {
        // Given
        when(merchantRepository.findById("test-merchant-id")).thenThrow(new RuntimeException("Database error"));

        // When
        eventListener.handleMerchantBalanceUpdateEvent(testEvent);

        // Then
        verify(merchantRepository, never()).save(any(Merchant.class));
    }
}
