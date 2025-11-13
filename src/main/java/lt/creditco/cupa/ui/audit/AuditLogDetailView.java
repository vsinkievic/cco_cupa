package lt.creditco.cupa.ui.audit;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.ui.util.JsonDisplayComponent;

/**
 * Vaadin view for viewing Audit Log details (read-only).
 */
@Route(value = "audit-logs/view", layout = MainLayout.class)
@PageTitle("Audit Log Details | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
public class AuditLogDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final AuditLogService auditLogService;
    private AuditLogDTO auditLog;
    private final VerticalLayout contentLayout = new VerticalLayout();
    
    public AuditLogDetailView(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        setSizeFull();
        setPadding(true);
        add(contentLayout);
    }
    
    @Override
    public void setParameter(BeforeEvent event, Long auditLogId) {
        AuditLogDTO auditLog = auditLogService.findOne(auditLogId).orElse(null);
        if (auditLog != null) {
            this.auditLog = auditLog;
            buildContent();
        } else {
            Notification.show("Audit log not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(AuditLogListView.class));
        }
    }
    
    private void buildContent() {
        contentLayout.removeAll();
        
        H2 title = new H2("Audit Log Details");
        Button backButton = new Button("Back", e -> getUI().ifPresent(ui -> ui.navigate(AuditLogListView.class)));
        HorizontalLayout header = new HorizontalLayout(title, backButton);
        header.setWidthFull();
        header.expand(title);
        
        Div details = new Div();
        details.add(createField("Timestamp", auditLog.getRequestTimestamp() != null ? auditLog.getRequestTimestamp().toString() : ""));
        details.add(createField("Endpoint", auditLog.getApiEndpoint()));
        details.add(createField("HTTP Method", auditLog.getHttpMethod()));
        details.add(createField("Merchant", auditLog.getMerchantId()));
        details.add(createField("Status Code", auditLog.getHttpStatusCode() != null ? auditLog.getHttpStatusCode().toString() : ""));
        
        H3 requestTitle = new H3("Request Body");
        JsonDisplayComponent requestJson = new JsonDisplayComponent();
        requestJson.setJsonContent(auditLog.getRequestData());
        
        H3 responseTitle = new H3("Response Body");
        JsonDisplayComponent responseJson = new JsonDisplayComponent();
        responseJson.setJsonContent(auditLog.getResponseData());
        
        contentLayout.add(header, details, requestTitle, requestJson, responseTitle, responseJson);
    }
    
    private Div createField(String label, String value) {
        Div fieldDiv = new Div();
        fieldDiv.getStyle().set("margin-bottom", "10px");
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("font-weight", "bold");
        Span valueSpan = new Span(value != null ? value : "");
        fieldDiv.add(labelSpan, valueSpan);
        return fieldDiv;
    }
}

