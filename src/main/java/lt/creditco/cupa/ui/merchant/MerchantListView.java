package lt.creditco.cupa.ui.merchant;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.MerchantDTO;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Vaadin view for listing Merchants.
 */
@Route(value = "merchants", layout = MainLayout.class)
@PageTitle("Merchants | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT })
public class MerchantListView extends VerticalLayout {

    private final MerchantService merchantService;
    
    private final Grid<MerchantDTO> grid = new Grid<>(MerchantDTO.class, false);
    
    private final TextField nameFilter = new TextField("Name");
    private final ComboBox<MerchantMode> modeFilter = new ComboBox<>("Mode");
    private final ComboBox<MerchantStatus> statusFilter = new ComboBox<>("Status");
    private final CupaUser loggedInUser;
    private final CupaUserService cupaUserService;
    
    public MerchantListView(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        
        add(createToolbar(), createGrid());
        
        refreshGrid();
    }
    
    private HorizontalLayout createToolbar() {
        // Filters
        nameFilter.setPlaceholder("Filter by name");
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(e -> refreshGrid());
        
        modeFilter.setItems(MerchantMode.values());
        modeFilter.setPlaceholder("All modes");
        modeFilter.setClearButtonVisible(true);
        modeFilter.addValueChangeListener(e -> refreshGrid());
        
        statusFilter.setItems(MerchantStatus.values());
        statusFilter.setPlaceholder("All statuses");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> refreshGrid());
        
        // Create button
        Button createButton = new Button("New Merchant", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> { 
            getUI().ifPresent(ui -> ui.navigate(MerchantNewRouteHandler.class)); 
        });

        
        HorizontalLayout toolbar = new HorizontalLayout(nameFilter, modeFilter, statusFilter, createButton);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setWidthFull();
        toolbar.expand(nameFilter);
        
        return toolbar;
    }
    
    private Grid<MerchantDTO> createGrid() {
        grid.addColumn(MerchantDTO::getId).setHeader("ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(MerchantDTO::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(MerchantDTO::getMode).setHeader("Mode").setSortable(true).setAutoWidth(true);
        grid.addColumn(MerchantDTO::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true);
        grid.addColumn(MerchantDTO::getBalance).setHeader("Balance").setSortable(true).setAutoWidth(true);
        grid.addColumn(MerchantDTO::getCurrency).setHeader("Currency").setSortable(true).setAutoWidth(true);
        
        // Action buttons
        grid.addComponentColumn(merchant -> {
            RouterLink viewLink = new RouterLink("", MerchantDetailView.class, merchant.getId());
            viewLink.add(VaadinIcon.EYE.create());
            viewLink.addClassNames("button", "button-tertiary-inline", "button-small");
            viewLink.getElement().setAttribute("title", "View Merchant");
            return viewLink;
        }).setHeader(" ").setWidth("70px").setFlexGrow(0);
        
        // Add double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(MerchantDetailView.class, event.getItem().getId()))
        );
        
        grid.setSizeFull();
        
        return grid;
    }
    
    private void refreshGrid() {
        // Load all accessible merchants (with pagination to limit initial load)
        var pageable = PageRequest.of(0, 1000);
        List<MerchantDTO> allMerchants = merchantService.findAllWithAccessControl(pageable, loggedInUser).getContent();
        
        // Create ListDataProvider with the loaded data
        ListDataProvider<MerchantDTO> dataProvider = new ListDataProvider<>(allMerchants);
        
        // Configure filters
        dataProvider.setFilter(merchant -> {
            String nameValue = nameFilter.getValue();
            MerchantMode modeValue = modeFilter.getValue();
            MerchantStatus statusValue = statusFilter.getValue();
            
            // Name filter
            if (nameValue != null && !nameValue.isEmpty() && 
                (merchant.getName() == null || !merchant.getName().toLowerCase().contains(nameValue.toLowerCase()))) {
                return false;
            }
            
            // Mode filter
            if (modeValue != null && merchant.getMode() != modeValue) {
                return false;
            }
            
            // Status filter
            if (statusValue != null && merchant.getStatus() != statusValue) {
                return false;
            }
            
            return true;
        });
        
        grid.setDataProvider(dataProvider);
    }
}

