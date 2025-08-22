package lt.creditco.cupa.domain.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MergerTest {

    @Test
    void testBasicMerge() {
        TestEntity entity = new TestEntity();
        entity.setName("Old Name");
        entity.setValue(100);

        Merger<TestEntity> merger = Merger.of(entity);

        merger.merge("Name", entity::getName, "New Name", entity::setName).merge("Value", entity::getValue, 200, entity::setValue);

        assertTrue(merger.hasChanges());
        assertEquals("Name ('Old Name'->'New Name'), Value ('100'->'200')", merger.getChangeLog());
        assertEquals("New Name", entity.getName());
        assertEquals(200, entity.getValue());
    }

    @Test
    void testNoChanges() {
        TestEntity entity = new TestEntity();
        entity.setName("Same Name");
        entity.setValue(100);

        Merger<TestEntity> merger = Merger.of(entity);

        merger.merge("Name", entity::getName, "Same Name", entity::setName).merge("Value", entity::getValue, 100, entity::setValue);

        assertFalse(merger.hasChanges());
        assertEquals("", merger.getChangeLog());
    }

    @Test
    void testNullValues() {
        TestEntity entity = new TestEntity();
        entity.setName("Old Name");
        entity.setValue(100);

        Merger<TestEntity> merger = Merger.of(entity);

        merger.merge("Name", entity::getName, null, entity::setName).merge("Value", entity::getValue, null, entity::setValue);

        assertFalse(merger.hasChanges());
        assertEquals("", merger.getChangeLog());
    }

    @Test
    void testBigDecimalMerge() {
        TestEntity entity = new TestEntity();
        entity.setAmount(new BigDecimal("100.00"));
        entity.setBalance(new BigDecimal("50.000"));

        Merger<TestEntity> merger = Merger.of(entity);

        merger
            .mergeBigDecimal("Amount", entity::getAmount, new BigDecimal("200.00"), entity::setAmount)
            .mergeBigDecimal("Balance", entity::getBalance, new BigDecimal("75.000"), entity::setBalance);

        assertTrue(merger.hasChanges());
        // The stripTrailingZeros() method converts to scientific notation for clean representation
        assertEquals("Amount ('1E+2'->'2E+2'), Balance ('5E+1'->'75')", merger.getChangeLog());
        assertEquals(new BigDecimal("200.00"), entity.getAmount());
        assertEquals(new BigDecimal("75.000"), entity.getBalance());
    }

    @Test
    void testBigDecimalNoChanges() {
        TestEntity entity = new TestEntity();
        entity.setAmount(new BigDecimal("100.00"));
        entity.setBalance(new BigDecimal("50.000"));

        Merger<TestEntity> merger = Merger.of(entity);

        merger
            .mergeBigDecimal("Amount", entity::getAmount, new BigDecimal("100.00"), entity::setAmount)
            .mergeBigDecimal("Balance", entity::getBalance, new BigDecimal("50.000"), entity::setBalance);

        assertFalse(merger.hasChanges());
        assertEquals("", merger.getChangeLog());
    }

    @Test
    void testInstantMerge() {
        TestEntity entity = new TestEntity();
        Instant oldTime = Instant.parse("2024-01-01T10:00:00Z");
        Instant newTime = Instant.parse("2024-01-01T11:00:00Z");
        entity.setTimestamp(oldTime);

        Merger<TestEntity> merger = Merger.of(entity);

        merger.merge("Timestamp", entity::getTimestamp, newTime, entity::setTimestamp);

        assertTrue(merger.hasChanges());
        assertEquals("Timestamp ('2024-01-01T10:00:00Z'->'2024-01-01T11:00:00Z')", merger.getChangeLog());
        assertEquals(newTime, entity.getTimestamp());
    }

    // Simple test entity for testing the Merger utility
    private static class TestEntity {

        private String name;
        private Integer value;
        private BigDecimal amount;
        private BigDecimal balance;
        private Instant timestamp;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }
    }
}
