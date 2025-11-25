package lt.creditco.cupa.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lt.creditco.cupa.domain.Merchant;
import lt.creditco.cupa.repository.MerchantRepository;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.mapper.MerchantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for MerchantService prefix validation logic.
 * Tests all scenarios for prefix usage and uniqueness validation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MerchantServicePrefixValidationTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantMapper merchantMapper;

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantService(merchantRepository, merchantMapper);
    }

    /**
     * Scenario 1: No prefixes at all - should be valid.
     */
    @Test
    void testNoPrefixes_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        // All prefixes are null by default

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Scenario 2: One prefix set - should be valid.
     */
    @Test
    void testSinglePrefix_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("PREFIX1");

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Scenario 3: Same prefix for client ID and order ID in TEST, 
     * another one for both fields in LIVE - should be valid.
     */
    @Test
    void testSamePrefixForTestFields_differentForLive_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("TEST_PREFIX");
        dto.setTestOrderIdPrefix("TEST_PREFIX");  // Same as test client
        dto.setLiveClientIdPrefix("LIVE_PREFIX");
        dto.setLiveOrderIdPrefix("LIVE_PREFIX");  // Same as live client

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Scenario 4: Same prefix for client ID in both TEST and LIVE,
     * another one for order ID in both TEST and LIVE - should be valid.
     */
    @Test
    void testSamePrefixForClientAcrossEnvironments_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("CLIENT_PREFIX");
        dto.setLiveClientIdPrefix("CLIENT_PREFIX");  // Same as test client
        dto.setTestOrderIdPrefix("ORDER_PREFIX");
        dto.setLiveOrderIdPrefix("ORDER_PREFIX");    // Same as test order

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Scenario 5: Same prefix for all four fields - should be valid.
     */
    @Test
    void testSamePrefixForAllFields_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("SAME_PREFIX");
        dto.setTestOrderIdPrefix("SAME_PREFIX");
        dto.setLiveClientIdPrefix("SAME_PREFIX");
        dto.setLiveOrderIdPrefix("SAME_PREFIX");

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Test: Prefix conflict with another merchant - should fail.
     */
    @Test
    void testPrefixConflictWithAnotherMerchant_shouldThrowException() {
        // Given
        MerchantDTO newMerchant = createMerchantDTO("merchant2");
        newMerchant.setTestClientIdPrefix("CONFLICT_PREFIX");

        // Existing merchant already uses this prefix
        Merchant existingMerchant = new Merchant();
        existingMerchant.id("merchant1");
        existingMerchant.setTestOrderIdPrefix("CONFLICT_PREFIX");  // Uses the same prefix in different field

        when(merchantRepository.findAll()).thenReturn(List.of(existingMerchant));

        // When/Then
        assertThatThrownBy(() -> merchantService.save(newMerchant))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CONFLICT_PREFIX")
            .hasMessageContaining("already used by merchant: merchant1");
    }

    /**
     * Test: Updating existing merchant with same prefixes - should be valid.
     */
    @Test
    void testUpdateMerchantWithSamePrefixes_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("MY_PREFIX");
        dto.setLiveOrderIdPrefix("MY_PREFIX");  // Same prefix in different field

        // This is the same merchant being updated
        Merchant existingMerchant = new Merchant();
        existingMerchant.id("merchant1");
        existingMerchant.setTestClientIdPrefix("MY_PREFIX");

        when(merchantRepository.findAll()).thenReturn(List.of(existingMerchant));
        setupSuccessfulUpdate(dto, existingMerchant);

        // When/Then - should not throw (same merchant can keep its prefixes)
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Test: New merchant trying to use prefix from existing merchant - should fail.
     */
    @Test
    void testNewMerchantUsingExistingPrefix_shouldThrowException() {
        // Given
        MerchantDTO newMerchant = createMerchantDTO(null);  // New merchant has no ID
        newMerchant.setLiveClientIdPrefix("EXISTING_PREFIX");

        Merchant existingMerchant = new Merchant();
        existingMerchant.id("merchant1");
        existingMerchant.setTestClientIdPrefix("EXISTING_PREFIX");

        when(merchantRepository.findAll()).thenReturn(List.of(existingMerchant));

        // When/Then
        assertThatThrownBy(() -> merchantService.save(newMerchant))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("EXISTING_PREFIX")
            .hasMessageContaining("already used by merchant");
    }

    /**
     * Test: Multiple prefixes set, all different - should be valid.
     */
    @Test
    void testMultipleDifferentPrefixes_shouldBeValid() {
        // Given
        MerchantDTO dto = createMerchantDTO("merchant1");
        dto.setTestClientIdPrefix("PREFIX_A");
        dto.setTestOrderIdPrefix("PREFIX_B");
        dto.setLiveClientIdPrefix("PREFIX_C");
        dto.setLiveOrderIdPrefix("PREFIX_D");

        when(merchantRepository.findAll()).thenReturn(new ArrayList<>());
        setupSuccessfulSave(dto);

        // When/Then - should not throw
        assertThatCode(() -> merchantService.save(dto)).doesNotThrowAnyException();
    }

    /**
     * Test: Conflict check is case-sensitive.
     */
    @Test
    void testPrefixConflictIsCaseSensitive_shouldAllowDifferentCase() {
        // Given
        MerchantDTO newMerchant = createMerchantDTO("merchant2");
        newMerchant.setTestClientIdPrefix("prefix");  // lowercase

        Merchant existingMerchant = new Merchant();
        existingMerchant.id("merchant1");
        existingMerchant.setTestClientIdPrefix("PREFIX");  // uppercase - different!

        when(merchantRepository.findAll()).thenReturn(List.of(existingMerchant));
        setupSuccessfulSave(newMerchant);

        // When/Then - should not throw (case-sensitive comparison)
        assertThatCode(() -> merchantService.save(newMerchant)).doesNotThrowAnyException();
    }

    /**
     * Helper method to create a MerchantDTO with basic required fields.
     */
    private MerchantDTO createMerchantDTO(String id) {
        MerchantDTO dto = new MerchantDTO();
        dto.setId(id);
        dto.setName("Test Merchant");
        dto.setBalance(BigDecimal.ZERO);
        return dto;
    }

    /**
     * Helper to set up successful save operation.
     */
    private void setupSuccessfulSave(MerchantDTO dto) {
        Merchant entity = new Merchant();
        entity.id(dto.getId());
        when(merchantMapper.toEntity(dto)).thenReturn(entity);
        when(merchantRepository.save(entity)).thenReturn(entity);
        when(merchantMapper.toDto(entity)).thenReturn(dto);
    }

    /**
     * Helper to set up successful update operation.
     */
    private void setupSuccessfulUpdate(MerchantDTO dto, Merchant entity) {
        when(merchantMapper.toEntity(dto)).thenReturn(entity);
        when(merchantRepository.save(entity)).thenReturn(entity);
        when(merchantMapper.toDto(entity)).thenReturn(dto);
    }
}

