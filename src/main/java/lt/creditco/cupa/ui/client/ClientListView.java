package lt.creditco.cupa.ui.client;

import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Vaadin view for listing Clients.
 */
@Route(value = "clients", layout = MainLayout.class)
@PageTitle("Clients | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientListView extends VerticalLayout {

    private final ClientService clientService;
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Grid<ClientDTO> grid = new Grid<>(ClientDTO.class, false);
    
    private final TextField clientIdFilter = new TextField("Client ID");
    private final ComboBox<MerchantDTO> merchantFilter = new ComboBox<>("Merchant");
    private final TextField nameFilter = new TextField("Name");
    private final TextField emailFilter = new TextField("Email");
    private final ComboBox<MerchantMode> environmentFilter = new ComboBox<>("Environment");
    
    public ClientListView(ClientService clientService, MerchantService merchantService, CupaUserService cupaUserService) {
        this.clientService = clientService;
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        
        BreadcrumbBar breadcrumbBar = new BreadcrumbBar(
            Breadcrumbs.builder()
                .home()
                .currentLink("Clients", ClientListView.class)
                .build()
        );
        
        add(breadcrumbBar, createToolbar(), createGrid());
        
        loadMerchants();
        refreshGrid();
    }
    
    private void loadMerchants() {
        log.debug("Loading merchants for user: {}", loggedInUser.getLogin());
        var merchants = merchantService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser).getContent();
        merchantFilter.setItems(merchants);
        merchantFilter.setItemLabelGenerator(merchant -> merchant.getId() + " - " + merchant.getName());
    }
    
    private HorizontalLayout createToolbar() {
        // Filters
        clientIdFilter.setPlaceholder("Client ID");
        clientIdFilter.setClearButtonVisible(true);
        clientIdFilter.setValueChangeMode(ValueChangeMode.LAZY);
        clientIdFilter.addValueChangeListener(e -> refreshGrid());
        
        merchantFilter.setPlaceholder("All merchants");
        merchantFilter.setClearButtonVisible(true);
        merchantFilter.addValueChangeListener(e -> refreshGrid());
        
        nameFilter.setPlaceholder("Filter by name");
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(e -> refreshGrid());
        
        emailFilter.setPlaceholder("Filter by email");
        emailFilter.setClearButtonVisible(true);
        emailFilter.setValueChangeMode(ValueChangeMode.LAZY);
        emailFilter.addValueChangeListener(e -> refreshGrid());
        
        // Environment filter
        environmentFilter.setPlaceholder("All environments");
        environmentFilter.setClearButtonVisible(true);
        environmentFilter.setItems(MerchantMode.values());
        environmentFilter.addValueChangeListener(e -> refreshGrid());
        
        // Create button
        Button createButton = new Button("New Client", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ClientNewRouteHandler.class)));
        
        HorizontalLayout toolbar = new HorizontalLayout(clientIdFilter, nameFilter, emailFilter, merchantFilter, environmentFilter, createButton);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setWidthFull();
        toolbar.expand(nameFilter);
        
        return toolbar;
    }
    
    private Grid<ClientDTO> createGrid() {
        grid.addColumn(ClientDTO::getMerchantClientId).setHeader("Merchant Client ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientDTO::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientDTO::getEmailAddress).setHeader("Email").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientDTO::getMobileNumber).setHeader("Phone").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientDTO::getMerchantId).setHeader("Merchant").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientDTO::getEnvironment).setHeader("Env").setSortable(true).setWidth("80px");
        
        // Action buttons - use RouterLink to enable URL preview and "Open in Tab"
        grid.addComponentColumn(client -> {
            RouterLink viewLink = new RouterLink("", ClientDetailView.class, client.getId());
            viewLink.add(VaadinIcon.EYE.create());
            viewLink.getElement().setAttribute("title", "View Client");
            return viewLink;
        }).setHeader(" ").setWidth("70px").setFlexGrow(0);
        
        // Add double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(ClientDetailView.class, event.getItem().getId()))
        );
        
        grid.setSizeFull();
        
        return grid;
    }
    
    private void refreshGrid() {
        log.debug("Refreshing clients grid for user: {}", loggedInUser.getLogin());
        
        // Load all accessible clients (with pagination to limit initial load)
        // For production with large datasets, consider implementing server-side filtering
        var pageable = PageRequest.of(0, 1000); // Adjust size based on expected data volume
        List<ClientDTO> allClients = clientService.findAllWithAccessControl(pageable, loggedInUser).getContent();
        
        // Create ListDataProvider with the loaded data
        ListDataProvider<ClientDTO> dataProvider = new ListDataProvider<>(allClients);
        
        // Configure filters
        dataProvider.setFilter(client -> {
            String clientIdValue = clientIdFilter.getValue();
            MerchantDTO merchantValue = merchantFilter.getValue();
            String nameValue = nameFilter.getValue();
            String emailValue = emailFilter.getValue();
            MerchantMode envValue = environmentFilter.getValue();
            
            // Client ID filter (exact match)
            if (clientIdValue != null && !clientIdValue.isEmpty() && 
                (client.getMerchantClientId() == null || !client.getMerchantClientId().equals(clientIdValue))) {
                return false;
            }
            
            // Merchant filter
            if (merchantValue != null && client.getMerchantId() != null && 
                !client.getMerchantId().equals(merchantValue.getId())) {
                return false;
            }
            
            // Name filter
            if (nameValue != null && !nameValue.isEmpty() && 
                (client.getName() == null || !client.getName().toLowerCase().contains(nameValue.toLowerCase()))) {
                return false;
            }
            
            // Email filter
            if (emailValue != null && !emailValue.isEmpty() && 
                (client.getEmailAddress() == null || !client.getEmailAddress().toLowerCase().contains(emailValue.toLowerCase()))) {
                return false;
            }
            
            // Environment filter
            if (envValue != null) {
                // Skip clients with null environment if a filter is applied
                if (client.getEnvironment() == null) {
                    return false;
                }
                // Skip clients that don't match the selected environment
                if (!client.getEnvironment().equals(envValue)) {
                    return false;
                }
            }
            
            return true;
        });
        
        grid.setDataProvider(dataProvider);
    }
}

