package lt.creditco.cupa.ui.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Simple component for displaying JSON data in a formatted way.
 * Automatically detects and pretty-prints valid JSON content.
 */
public class JsonDisplayComponent extends VerticalLayout {

    /**
     * Shared ObjectMapper instance for JSON formatting.
     * This is only used for parsing and pretty-printing already-serialized JSON strings,
     * NOT for object serialization/deserialization, so a simple configuration is sufficient.
     * The mapper preserves all data as-is since we're working with generic Object.class.
     */
    private static final ObjectMapper OBJECT_MAPPER;
    
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        // Preserve data integrity by not modifying any values during parse/reformat
        OBJECT_MAPPER.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }
    
    private final TextArea jsonTextArea = new TextArea();
    private int minRows = 3;
    private int maxRows = 10;
    
    public JsonDisplayComponent() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        
        jsonTextArea.setWidthFull();
        jsonTextArea.setReadOnly(true);
        jsonTextArea.getStyle().set("font-family", "monospace");
        
        // Set default height range
        applyHeightRange();
        
        add(jsonTextArea);
    }
    
    /**
     * Sets the height range for the text area based on row counts.
     * The component will adapt its height between min and max rows based on content.
     * 
     * @param minRows minimum number of visible rows (default: 3)
     * @param maxRows maximum number of visible rows (default: 10)
     * @return this component for method chaining
     */
    public JsonDisplayComponent setRowRange(int minRows, int maxRows) {
        if (minRows < 1) {
            throw new IllegalArgumentException("minRows must be at least 1");
        }
        if (maxRows < minRows) {
            throw new IllegalArgumentException("maxRows must be greater than or equal to minRows");
        }
        this.minRows = minRows;
        this.maxRows = maxRows;
        applyHeightRange();
        return this;
    }
    
    /**
     * Applies the configured height range to the text area.
     * Uses CSS to set min-height and max-height based on row counts.
     * Approximate calculation: 1 row ≈ 1.5em
     */
    private void applyHeightRange() {
        // Calculate heights: each row is approximately 1.5em, plus padding
        String minHeight = String.format("calc(%dem + 1em)", (int)(minRows * 1.5));
        String maxHeight = String.format("calc(%dem + 1em)", (int)(maxRows * 1.5));
        
        jsonTextArea.getStyle()
            .set("min-height", minHeight)
            .set("max-height", maxHeight)
            .set("overflow-y", "auto")
            .set("resize", "vertical");
    }
    
    public void setJsonContent(String json) {
        if (json == null || json.isBlank()) {
            jsonTextArea.setValue("");
        } else {
            // Ensure we're working with a String and not an object reference
            String jsonString = String.valueOf(json);
            
            // Try to format as JSON
            String formattedJson = formatJsonIfValid(jsonString);
            jsonTextArea.setValue(formattedJson);
        }
    }
    
    /**
     * Attempts to parse and pretty-print JSON content.
     * If the content is not valid JSON, returns the original string.
     * 
     * @param content the content to format
     * @return formatted JSON if valid, otherwise original content
     */
    private String formatJsonIfValid(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // Quick check: JSON typically starts with { or [
        String trimmed = content.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return content;
        }
        
        try {
            // Parse and re-serialize with indentation
            Object jsonObject = OBJECT_MAPPER.readValue(trimmed, Object.class);
            return OBJECT_MAPPER.writeValueAsString(jsonObject);
        } catch (Exception e) {
            // Not valid JSON or parsing error, return original
            return content;
        }
    }
}



