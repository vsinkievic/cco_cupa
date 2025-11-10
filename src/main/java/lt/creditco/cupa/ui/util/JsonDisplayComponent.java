package lt.creditco.cupa.ui.util;

import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Simple component for displaying JSON data in a formatted way.
 */
public class JsonDisplayComponent extends VerticalLayout {

    private final Pre jsonPre = new Pre();
    
    public JsonDisplayComponent() {
        setPadding(false);
        setSpacing(false);
        
        jsonPre.getStyle()
            .set("background-color", "#f5f5f5")
            .set("padding", "10px")
            .set("border-radius", "4px")
            .set("overflow-x", "auto")
            .set("max-height", "400px")
            .set("overflow-y", "auto");
        
        add(jsonPre);
    }
    
    public void setJsonContent(String json) {
        if (json == null || json.isEmpty()) {
            jsonPre.setText("No data");
        } else {
            // Simple formatting - just display as-is
            // Could be enhanced with proper JSON formatting library
            jsonPre.setText(json);
        }
    }
}



