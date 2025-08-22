package lt.creditco.cupa.remote;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom deserializer for Instant fields in gateway responses.
 * Treats dates without timezone information as UTC.
 */
public class GatewayInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // First try to parse as ISO instant (with timezone)
            return Instant.parse(dateStr);
        } catch (DateTimeParseException e) {
            // If that fails, try to parse as LocalDateTime and convert to UTC
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateStr);
                return localDateTime.toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                // If that also fails, try with a more flexible formatter
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
                    return localDateTime.toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException e3) {
                    // Last resort: try without milliseconds
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
                        return localDateTime.toInstant(ZoneOffset.UTC);
                    } catch (DateTimeParseException e4) {
                        throw new IOException("Unable to parse date: " + dateStr, e4);
                    }
                }
            }
        }
    }
}
