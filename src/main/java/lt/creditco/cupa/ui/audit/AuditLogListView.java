package lt.creditco.cupa.ui.audit;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Vaadin view for listing Audit Logs (read-only).
 */
@Route(value = "audit-logs", layout = MainLayout.class)
@PageTitle("Audit Logs | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
public class AuditLogListView extends VerticalLayout {

    private final AuditLogService auditLogService;
    private final Grid<AuditLogDTO> grid = new Grid<>(AuditLogDTO.class, false);
    
    public AuditLogListView(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        
        setSizeFull();
        setPadding(true);
        
        add(createGrid());
        refreshGrid();
    }
    
    private Grid<AuditLogDTO> createGrid() {
        grid.addColumn(AuditLogDTO::getRequestTimestamp).setHeader("Timestamp").setSortable(true).setAutoWidth(true);
        grid.addColumn(AuditLogDTO::getApiEndpoint).setHeader("Endpoint").setSortable(true).setAutoWidth(true);
        grid.addColumn(AuditLogDTO::getHttpMethod).setHeader("Method").setSortable(true).setAutoWidth(true);
        grid.addColumn(AuditLogDTO::getMerchantId).setHeader("Merchant").setSortable(true).setAutoWidth(true);
        grid.addColumn(AuditLogDTO::getHttpStatusCode).setHeader("Status").setSortable(true).setAutoWidth(true);
        
        grid.addComponentColumn(log -> {
            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            viewButton.setTooltipText("View Audit Log");
            viewButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(AuditLogDetailView.class, log.getId())));
            return viewButton;
        }).setHeader("Actions").setWidth("100px").setFlexGrow(0);
        
        // Add double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(AuditLogDetailView.class, event.getItem().getId()))
        );
        
        grid.setSizeFull();
        return grid;
    }
    
    private void refreshGrid() {
        // Load all audit logs (with pagination to limit initial load)
        var pageable = PageRequest.of(0, 1000);
        List<AuditLogDTO> allLogs = auditLogService.findAll(pageable).getContent();
        
        ListDataProvider<AuditLogDTO> dataProvider = new ListDataProvider<>(allLogs);
        grid.setDataProvider(dataProvider);
    }
}

