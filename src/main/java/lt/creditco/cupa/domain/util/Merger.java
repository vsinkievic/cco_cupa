package lt.creditco.cupa.domain.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A reusable utility to merge fields from a source object to a target object,
 * tracking changes and building a descriptive log.
 *
 * @param <T> The type of the target entity/object being updated.
 */
public class Merger<T> {

    private final T target;
    private final List<String> changeDescriptions = new ArrayList<>();
    private boolean wasChanged = false;

    private Merger(T target) {
        this.target = target;
    }

    /**
     * Factory method to start a merge operation for a given target object.
     * @param target The object to merge changes into.
     * @return A new Merger instance.
     */
    public static <T> Merger<T> of(T target) {
        return new Merger<>(target);
    }

    /**
     * Merges a single field.
     *
     * @param fieldName     The name of the field for logging purposes (e.g., "Status").
     * @param getter        A function to get the current value from the target.
     * @param newValue      The potential new value.
     * @param setter        A consumer to set the new value on the target if it has changed.
     * @param <V>           The type of the value being merged.
     * @return The current Merger instance for chaining.
     */
    public <V> Merger<T> merge(String fieldName, Supplier<V> getter, V newValue, Consumer<V> setter) {
        V oldValue = getter.get();

        // Check if the new value is different from the old one, ignoring null new values.
        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            setter.accept(newValue);
            this.wasChanged = true;
            addChangeDescription(fieldName, oldValue, newValue);
        }
        return this;
    }

    // Overload for when the value is a wrapped Type-Safe ID
    public <V> Merger<T> mergeId(String fieldName, Supplier<V> getter, V newValue, Consumer<V> setter, Function<V, Long> idExtractor) {
        V oldValue = getter.get();
        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            setter.accept(newValue);
            this.wasChanged = true;
            // Use the extractor to get the raw value for logging
            addChangeDescription(fieldName, idExtractor.apply(oldValue), idExtractor.apply(newValue));
        }
        return this;
    }

    public Merger<T> mergeBigDecimal(String fieldName, Supplier<BigDecimal> getter, BigDecimal newValue, Consumer<BigDecimal> setter) {
        BigDecimal oldValue = getter.get();

        // Normalize values for comparison and logging
        BigDecimal normalizedOldValue = _stripZeros(oldValue);
        BigDecimal normalizedNewValue = _stripZeros(newValue);

        if (normalizedNewValue != null && !Objects.equals(normalizedOldValue, normalizedNewValue)) {
            // Important: Set the original new value to preserve the intended scale
            setter.accept(newValue);
            this.wasChanged = true;
            // Use the normalized values for clean, canonical logging
            addChangeDescription(fieldName, normalizedOldValue, normalizedNewValue);
        }
        return this;
    }

    public boolean hasChanges() {
        return this.wasChanged;
    }

    public String getChangeLog() {
        return String.join(", ", this.changeDescriptions);
    }

    private <V> void addChangeDescription(String fieldName, V from, V to) {
        this.changeDescriptions.add(String.format("%s ('%s'->'%s')", fieldName, from, to));
    }

    private BigDecimal _stripZeros(BigDecimal value) {
        if (value == null) return null;
        else return value.stripTrailingZeros();
    }
}
