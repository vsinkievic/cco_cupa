package lt.creditco.cupa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DailyAmountLimitTest {

    @Test
    void testEquals_sameValues_shouldBeEqual() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        BigDecimal startAmount = BigDecimal.valueOf(100);
        LocalDate afterDate = LocalDate.of(2025, 2, 1);
        BigDecimal afterAmount = BigDecimal.valueOf(1000);

        DailyAmountLimit limit1 = new DailyAmountLimit();
        limit1.setStartDate(startDate);
        limit1.setStartAmount(startAmount);
        limit1.setAfterDate(afterDate);
        limit1.setAfterAmount(afterAmount);

        DailyAmountLimit limit2 = new DailyAmountLimit();
        limit2.setStartDate(startDate);
        limit2.setStartAmount(startAmount);
        limit2.setAfterDate(afterDate);
        limit2.setAfterAmount(afterAmount);

        assertThat(limit1).isEqualTo(limit2);
        assertThat(limit1.hashCode()).isEqualTo(limit2.hashCode());
    }

    @Test
    void testEquals_differentValues_shouldNotBeEqual() {
        DailyAmountLimit limit1 = new DailyAmountLimit();
        limit1.setStartDate(LocalDate.of(2025, 1, 1));
        limit1.setStartAmount(BigDecimal.valueOf(100));

        DailyAmountLimit limit2 = new DailyAmountLimit();
        limit2.setStartDate(LocalDate.of(2025, 1, 2));
        limit2.setStartAmount(BigDecimal.valueOf(200));

        assertThat(limit1).isNotEqualTo(limit2);
    }

    @Test
    void testEquals_withNull_shouldNotBeEqual() {
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterDate(LocalDate.of(2025, 1, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        assertThat(limit).isNotEqualTo(null);
    }

    @Test
    void testEquals_withDifferentClass_shouldNotBeEqual() {
        DailyAmountLimit limit = new DailyAmountLimit();
        assertThat(limit).isNotEqualTo("not a DailyAmountLimit");
    }

    @Test
    void testEquals_withSelf_shouldBeEqual() {
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterDate(LocalDate.of(2025, 1, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        assertThat(limit).isEqualTo(limit);
    }

    @Test
    void testToString_shouldContainAllFields() {
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setStartAmount(BigDecimal.valueOf(100));
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        String result = limit.toString();

        assertThat(result).contains("2025-01-01");
        assertThat(result).contains("100");
        assertThat(result).contains("2025-02-01");
        assertThat(result).contains("1000");
    }

    // ========== Validation Tests ==========

    @Test
    void shouldNotThrowException_whenAllFieldsAreNull() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();

        // when / then
        assertThatCode(() -> limit.validate("TEST"))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowException_whenOnlyAfterDateAndAmountAreSet() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatCode(() -> limit.validate("TEST"))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowException_whenAllFieldsAreSet() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setStartAmount(BigDecimal.valueOf(100));
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatCode(() -> limit.validate("TEST"))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowException_whenZeroAmountsAreUsed() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setStartAmount(BigDecimal.ZERO);
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.ZERO);

        // when / then
        assertThatCode(() -> limit.validate("TEST"))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowException_whenOnlyStartDateAndAmountAreSetWithoutAfterFields() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setStartAmount(BigDecimal.valueOf(100));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("if start date/amount is set, after date/amount must also be set");
    }

    @Test
    void shouldThrowException_whenStartDateIsSetButStartAmountIsMissing() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("start date is set but start amount is missing");
    }

    @Test
    void shouldThrowException_whenStartAmountIsSetButStartDateIsMissing() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartAmount(BigDecimal.valueOf(100));
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("start amount is set but start date is missing");
    }

    @Test
    void shouldThrowException_whenAfterDateIsSetButAfterAmountIsMissing() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterDate(LocalDate.of(2025, 2, 1));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("after date is set but after amount is missing");
    }

    @Test
    void shouldThrowException_whenAfterAmountIsSetButAfterDateIsMissing() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("after amount is set but after date is missing");
    }

    @Test
    void shouldThrowException_whenStartAmountIsNegative() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 1, 1));
        limit.setStartAmount(BigDecimal.valueOf(-100));
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("start amount must be greater than or equal to 0");
    }

    @Test
    void shouldThrowException_whenAfterAmountIsNegative() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterDate(LocalDate.of(2025, 2, 1));
        limit.setAfterAmount(BigDecimal.valueOf(-1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("after amount must be greater than or equal to 0");
    }

    @Test
    void shouldThrowException_whenAfterDateIsBeforeStartDate() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(LocalDate.of(2025, 2, 1));
        limit.setStartAmount(BigDecimal.valueOf(100));
        limit.setAfterDate(LocalDate.of(2025, 1, 1));
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("after date must be after start date");
    }

    @Test
    void shouldThrowException_whenAfterDateEqualsStartDate() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        LocalDate sameDate = LocalDate.of(2025, 1, 1);
        limit.setStartDate(sameDate);
        limit.setStartAmount(BigDecimal.valueOf(100));
        limit.setAfterDate(sameDate);
        limit.setAfterAmount(BigDecimal.valueOf(1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("TEST"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TEST daily amount limit")
            .hasMessageContaining("after date must be after start date");
    }

    @Test
    void shouldIncludeEnvironmentInErrorMessage_whenValidationFailsForLiveEnvironment() {
        // given
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setAfterAmount(BigDecimal.valueOf(-1000));

        // when / then
        assertThatThrownBy(() -> limit.validate("LIVE"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("LIVE daily amount limit");
    }
}


