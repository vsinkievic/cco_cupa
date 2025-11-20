package lt.creditco.cupa.ui.audit;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.AuditLogService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.AuditLogDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/**
 * Vaadin view for listing Audit Logs with filtering support.
 */
@Route(value = "audit-logs", layout = MainLayout.class)
@PageTitle("Audit Logs | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class AuditLogListView extends VerticalLayout {

    private final AuditLogService auditLogService;
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    private final Grid<AuditLogDTO> grid = new Grid<>(AuditLogDTO.class, false);
    private Grid.Column<AuditLogDTO> timestampColumn;

    // Filter components
    private final TextField orderIdFilter = new TextField("Order ID");
    private final ComboBox<String> methodFilter = new ComboBox<>("HTTP Method");
    private final TextField endpointFilter = new TextField("Endpoint");
    private final MultiSelectComboBox<Integer> statusCodeFilter = new MultiSelectComboBox<>("Status Code");
    private final MultiSelectComboBox<MerchantDTO> merchantFilter;
    private final ComboBox<MerchantMode> environmentFilter = new ComboBox<>("Environment");
    private final Button applyFiltersButton = new Button("Apply Filters");
    private final Button clearFiltersButton = new Button("Clear");

    public AuditLogListView(
        AuditLogService auditLogService,
        MerchantService merchantService,
        CupaUserService cupaUserService
    ) {
        this.auditLogService = auditLogService;
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
            .map(CupaUser.class::cast)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        this.merchantFilter = new MultiSelectComboBox<>("Merchant");
        
        setSizeFull();
        setPadding(true);
        
        add(createFilterToolbar());
        add(createGrid());
        loadFilterData();
        refreshGrid();
    }

    private HorizontalLayout createFilterToolbar() {
        orderIdFilter.setPlaceholder("Filter by order ID...");
        orderIdFilter.setClearButtonVisible(true);
        orderIdFilter.setWidth("200px");
        
        methodFilter.setPlaceholder("All methods");
        methodFilter.setClearButtonVisible(true);
        methodFilter.setWidth("150px");
        
        endpointFilter.setPlaceholder("Filter by endpoint...");
        endpointFilter.setClearButtonVisible(true);
        endpointFilter.setWidth("250px");
        
        statusCodeFilter.setPlaceholder("All status codes");
        statusCodeFilter.setWidth("150px");
        
        merchantFilter.setPlaceholder("All merchants");
        merchantFilter.setItemLabelGenerator(merchant -> merchant.getId() + " - " + merchant.getName());
        merchantFilter.setWidth("250px");
        
        environmentFilter.setPlaceholder("All environments");
        environmentFilter.setClearButtonVisible(true);
        environmentFilter.setWidth("150px");
        
        applyFiltersButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        applyFiltersButton.addClickListener(e -> applyFilters());
        clearFiltersButton.addClickListener(e -> clearFilters());
        
        HorizontalLayout toolbar = new HorizontalLayout(
            orderIdFilter, methodFilter, endpointFilter, 
            statusCodeFilter, merchantFilter, environmentFilter,
            applyFiltersButton, clearFiltersButton
        );
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setWidthFull();
        
        return toolbar;
    }

    private Grid<AuditLogDTO> createGrid() {
        grid.setSizeFull();
        
        // Add columns in the requested order: Timestamp, OrderID, method, endpoint, status, Merchant, environment, Actions
        timestampColumn = grid.addColumn(log -> 
            log.getRequestTimestamp() != null ? log.getRequestTimestamp().toString() : ""
        ).setHeader("Timestamp").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(AuditLogDTO::getOrderId)
            .setHeader("Order ID").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(AuditLogDTO::getHttpMethod)
            .setHeader("Method").setSortable(true).setWidth("100px");
        
        grid.addColumn(AuditLogDTO::getApiEndpoint)
            .setHeader("Endpoint").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(AuditLogDTO::getHttpStatusCode)
            .setHeader("Status").setSortable(true).setWidth("100px");
        
        grid.addColumn(AuditLogDTO::getMerchantId)
            .setHeader("Merchant").setSortable(true).setAutoWidth(true);
        
        grid.addColumn(AuditLogDTO::getEnvironment)
            .setHeader("Env").setSortable(true).setWidth("80px");
        
        // Actions column with RouterLink
        grid.addComponentColumn(log -> {
            RouterLink viewLink = new RouterLink("", AuditLogDetailView.class, log.getId());
            viewLink.add(VaadinIcon.EYE.create());
            viewLink.getElement().setAttribute("title", "View Audit Log");
            return viewLink;
        }).setHeader("").setWidth("70px").setFlexGrow(0);
        
        // Enable double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(AuditLogDetailView.class, event.getItem().getId()))
        );
        
        return grid;
    }

    private void loadFilterData() {
        // Load distinct HTTP methods
        methodFilter.setItems(auditLogService.findDistinctHttpMethods());
        
        // Load distinct status codes
        statusCodeFilter.setItems(auditLogService.findDistinctHttpStatusCodes());
        
        // Load merchants - service handles access control automatically
        var pageable = PageRequest.of(0, 1000);
        List<MerchantDTO> merchants = merchantService
            .findAllWithAccessControl(pageable, loggedInUser)
            .getContent();
        merchantFilter.setItems(merchants);
        
        // Load environment values
        environmentFilter.setItems(MerchantMode.values());
    }

    private void applyFilters() {
        // Convert empty strings to null for proper filtering
        String orderId = orderIdFilter.getValue();
        if (orderId != null && orderId.trim().isEmpty()) {
            orderId = null;
        }
        
        String endpoint = endpointFilter.getValue();
        if (endpoint != null && endpoint.trim().isEmpty()) {
            endpoint = null;
        }
        
        String method = methodFilter.getValue();
        MerchantMode environment = environmentFilter.getValue();
        
        Set<Integer> selectedStatusCodes = statusCodeFilter.getSelectedItems();
        List<Integer> statusCodes = selectedStatusCodes.isEmpty() ? null : 
            selectedStatusCodes.stream().toList();
        
        Set<MerchantDTO> selectedMerchants = merchantFilter.getSelectedItems();
        List<String> merchantIds = selectedMerchants.isEmpty() ? null : 
            selectedMerchants.stream().map(MerchantDTO::getId).toList();
        
        var pageable = PageRequest.of(0, 1000, 
            Sort.by(Sort.Direction.DESC, "requestTimestamp"));
        
        // Service handles ALL access control
        List<AuditLogDTO> filteredLogs = auditLogService
            .findByFiltersWithAccessControl(
                endpoint, method, orderId, environment != null ? environment.name() : null, statusCodes, merchantIds, pageable, loggedInUser
            )
            .getContent();
        
        grid.setItems(filteredLogs);
        applySortOrder();
    }

    private void refreshGrid() {
        var pageable = PageRequest.of(0, 1000, 
            Sort.by(Sort.Direction.DESC, "requestTimestamp"));
        
        List<AuditLogDTO> logs = auditLogService
            .findByFiltersWithAccessControl(null, null, null, null, null, null, pageable, loggedInUser)
            .getContent();
        
        grid.setItems(logs);
        applySortOrder();
    }

    private void applySortOrder() {
        grid.sort(java.util.Collections.singletonList(
            new GridSortOrder<>(timestampColumn, SortDirection.DESCENDING)
        ));
    }

    private void clearFilters() {
        orderIdFilter.clear();
        methodFilter.clear();
        endpointFilter.clear();
        statusCodeFilter.clear();
        merchantFilter.clear();
        environmentFilter.clear();
        refreshGrid();
    }
}
