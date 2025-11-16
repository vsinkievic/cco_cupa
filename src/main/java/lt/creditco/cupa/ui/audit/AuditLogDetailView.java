package lt.creditco.cupa.ui.audit;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.ui.util.JsonDisplayComponent;
import org.springframework.context.annotation.Scope;

/**
 * Vaadin view for viewing Audit Log details (read-only).
 */
@Route(value = "audit-logs", layout = MainLayout.class)
@PageTitle("Audit Log Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
public class AuditLogDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final AuditLogService auditLogService;
    protected final Binder<AuditLogDTO> binder = new Binder<>(AuditLogDTO.class);
    
    // Form fields
    private final TextField requestTimestampField = new TextField("Timestamp");
    private final TextField apiEndpointField = new TextField("Endpoint");
    private final TextField httpMethodField = new TextField("HTTP Method");
    private final TextField merchantIdField = new TextField("Merchant ID");
    private final IntegerField httpStatusCodeField = new IntegerField("Status Code");
    
    // JSON display components
    private final JsonDisplayComponent requestDataComponent = new JsonDisplayComponent();
    private final JsonDisplayComponent responseDataComponent = new JsonDisplayComponent();
    
    // Buttons
    private final Button backButton = new Button("Back");
    
    public AuditLogDetailView(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(createHeader());
        add(createFormLayout());
        add(createJsonSections());
        
        configureBinder();
        setReadOnlyMode();
    }
    
    private HorizontalLayout createHeader() {
        H2 title = new H2("Audit Log Details");
        
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate("audit-logs"))
        );
        
        HorizontalLayout header = new HorizontalLayout(title, backButton);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setWidthFull();
        header.expand(title);
        
        return header;
    }
    
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        requestTimestampField.setWidthFull();
        apiEndpointField.setWidthFull();
        httpMethodField.setWidthFull();
        merchantIdField.setWidthFull();
        httpStatusCodeField.setWidthFull();
        
        formLayout.add(requestTimestampField, apiEndpointField);
        formLayout.add(httpMethodField, merchantIdField);
        formLayout.add(httpStatusCodeField, 1);
        
        return formLayout;
    }
    
    private VerticalLayout createJsonSections() {
        VerticalLayout jsonLayout = new VerticalLayout();
        jsonLayout.setSpacing(true);
        jsonLayout.setPadding(false);
        
        H3 requestTitle = new H3("Request Body");
        H3 responseTitle = new H3("Response Body");
        
        jsonLayout.add(requestTitle, requestDataComponent, responseTitle, responseDataComponent);
        
        return jsonLayout;
    }
    
    private void configureBinder() {
        binder.forField(requestTimestampField)
            .bind(
                dto -> dto.getRequestTimestamp() != null ? dto.getRequestTimestamp().toString() : "",
                (dto, value) -> {}
            );
        binder.forField(apiEndpointField).bind(AuditLogDTO::getApiEndpoint, (dto, value) -> {});
        binder.forField(httpMethodField).bind(AuditLogDTO::getHttpMethod, (dto, value) -> {});
        binder.forField(merchantIdField).bind(AuditLogDTO::getMerchantId, (dto, value) -> {});
        binder.forField(httpStatusCodeField).bind(AuditLogDTO::getHttpStatusCode, (dto, value) -> {});
    }
    
    private void setReadOnlyMode() {
        binder.setReadOnly(true);
    }
    
    @Override
    public void setParameter(BeforeEvent event, Long logId) {
        if (logId == null) {
            Notification.show("Invalid audit log ID", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateBack();
            return;
        }
        
        AuditLogDTO auditLog = auditLogService.findOne(logId).orElse(null);
        if (auditLog != null) {
            binder.setBean(auditLog);
            
            // Set JSON content
            requestDataComponent.setJsonContent(auditLog.getRequestData());
            responseDataComponent.setJsonContent(auditLog.getResponseData());
        } else {
            Notification.show("Audit log not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateBack();
        }
    }
    
    private void navigateBack() {
        getUI().ifPresent(ui -> ui.navigate(AuditLogListView.class));
    }
}

