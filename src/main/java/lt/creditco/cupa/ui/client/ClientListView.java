package lt.creditco.cupa.ui.client;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.springframework.data.domain.PageRequest;

/**
 * Vaadin view for listing Clients.
 */
@Route(value = "clients", layout = MainLayout.class)
@PageTitle("Clients | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientListView extends VerticalLayout {

    private final ClientService clientService;
    private final MerchantService merchantService;
    
    private final Grid<ClientDTO> grid = new Grid<>(ClientDTO.class, false);
    
    private final ComboBox<MerchantDTO> merchantFilter = new ComboBox<>("Merchant");
    private final TextField nameFilter = new TextField("Name");
    private final TextField emailFilter = new TextField("Email");
    
    public ClientListView(ClientService clientService, MerchantService merchantService) {
        this.clientService = clientService;
        this.merchantService = merchantService;
        
        setSizeFull();
        setPadding(true);
        
        add(createToolbar(), createGrid());
        
        loadMerchants();
        refreshGrid();
    }
    
    private void loadMerchants() {
        var merchants = merchantService.findAll(PageRequest.of(0, 100)).getContent();
        merchantFilter.setItems(merchants);
        merchantFilter.setItemLabelGenerator(MerchantDTO::getName);
    }
    
    private HorizontalLayout createToolbar() {
        // Filters
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
        
        // Create button
        Button createButton = new Button("New Client", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ClientFormView.class, "new")));
        
        HorizontalLayout toolbar = new HorizontalLayout(merchantFilter, nameFilter, emailFilter, createButton);
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
        grid.addColumn(ClientDTO::getMerchantName).setHeader("Merchant").setSortable(true).setAutoWidth(true);
        
        // Action buttons
        grid.addComponentColumn(client -> {
            Button viewButton = new Button("View", VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewButton.addClickListener(e -> 
                getUI().ifPresent(ui -> ui.navigate(ClientDetailView.class, client.getId()))
            );
            
            Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> 
                getUI().ifPresent(ui -> ui.navigate(ClientFormView.class, client.getId()))
            );
            
            return new HorizontalLayout(viewButton, editButton);
        }).setHeader("Actions").setAutoWidth(true);
        
        grid.setSizeFull();
        
        return grid;
    }
    
    private void refreshGrid() {
        MerchantDTO merchantValue = merchantFilter.getValue();
        String nameValue = nameFilter.getValue();
        String emailValue = emailFilter.getValue();
        
        CallbackDataProvider<ClientDTO, Void> dataProvider = DataProvider.fromCallbacks(
            query -> {
                int page = query.getPage();
                int size = query.getPageSize();
                
                var pageable = PageRequest.of(page, size);
                var result = clientService.findAll(pageable);
                
                // Apply filters manually (could be optimized with service-level filtering)
                return result.stream()
                    .filter(c -> merchantValue == null || 
                                (c.getMerchantId() != null && c.getMerchantId().equals(merchantValue.getId())))
                    .filter(c -> nameValue == null || nameValue.isEmpty() || 
                                (c.getName() != null && c.getName().toLowerCase().contains(nameValue.toLowerCase())))
                    .filter(c -> emailValue == null || emailValue.isEmpty() || 
                                (c.getEmailAddress() != null && c.getEmailAddress().toLowerCase().contains(emailValue.toLowerCase())));
            },
            query -> {
                return (int) clientService.count();
            }
        );
        
        grid.setDataProvider(dataProvider);
    }
}

