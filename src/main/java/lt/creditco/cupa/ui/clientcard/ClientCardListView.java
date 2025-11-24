package lt.creditco.cupa.ui.clientcard;

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
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Vaadin view for listing Client Cards.
 */
@Route(value = "client-cards", layout = MainLayout.class)
@PageTitle("Client Cards | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientCardListView extends VerticalLayout {

    private final ClientCardService clientCardService;
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Grid<ClientCardDTO> grid = new Grid<>(ClientCardDTO.class, false);
    
    private final ComboBox<MerchantDTO> merchantFilter = new ComboBox<>("Merchant");
    private final TextField cardNumberFilter = new TextField("Card Number");
    
    public ClientCardListView(ClientCardService clientCardService, MerchantService merchantService, CupaUserService cupaUserService) {
        this.clientCardService = clientCardService;
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
                .currentLink("Client Cards", ClientCardListView.class)
                .build()
        );
        
        add(breadcrumbBar, createToolbar(), createGrid());
        
        loadMerchants();
        refreshGrid();
    }
    
    private void loadMerchants() {
        var merchants = merchantService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser).getContent();
        merchantFilter.setItems(merchants);
        merchantFilter.setItemLabelGenerator(MerchantDTO::getName);
    }
    
    private HorizontalLayout createToolbar() {
        // Filters
        merchantFilter.setPlaceholder("All merchants");
        merchantFilter.setClearButtonVisible(true);
        merchantFilter.addValueChangeListener(e -> refreshGrid());
        
        cardNumberFilter.setPlaceholder("Filter by card number");
        cardNumberFilter.setClearButtonVisible(true);
        cardNumberFilter.setValueChangeMode(ValueChangeMode.LAZY);
        cardNumberFilter.addValueChangeListener(e -> refreshGrid());
        
        // Create button - disabled (not tested yet, may not be used)
        Button createButton = new Button("New Card", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setEnabled(false);
//        createButton.setTooltipText("Feature not available - cards are managed by the payment gateway");
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ClientCardNewRouteHandler.class)));
        
        HorizontalLayout toolbar = new HorizontalLayout(merchantFilter, cardNumberFilter, createButton);
        toolbar.setDefaultVerticalComponentAlignment(Alignment.END);
        toolbar.setWidthFull();
        toolbar.expand(cardNumberFilter);
        
        return toolbar;
    }
    
    private Grid<ClientCardDTO> createGrid() {
        grid.addColumn(ClientCardDTO::getMaskedPan)
            .setHeader("Card Number").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClientCardDTO::getExpiryDate).setHeader("Expiry").setSortable(true).setAutoWidth(true);
        grid.addColumn(card -> {
            if (card.getClient() != null) {
                return card.getClient().getName();
            }
            return "";
        }).setHeader("Client").setSortable(true).setAutoWidth(true);
        grid.addColumn(card -> {
            if (card.getClient() != null) {
                return card.getClient().getMerchantName();
            }
            return "";
        }).setHeader("Merchant").setSortable(true).setAutoWidth(true);
        
        // Action buttons - use RouterLink to enable URL preview and "Open in Tab"
        grid.addComponentColumn(card -> {
            RouterLink viewLink = new RouterLink("", ClientCardDetailView.class, card.getId());
            viewLink.add(VaadinIcon.EYE.create());
            viewLink.getElement().setAttribute("title", "View Card");
            return viewLink;
        }).setHeader(" ").setWidth("70px").setFlexGrow(0);
        
        // Add double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(ClientCardDetailView.class, event.getItem().getId()))
        );
        
        grid.setSizeFull();
        
        return grid;
    }
    
    private void refreshGrid() {
        // Load all accessible client cards (with pagination to limit initial load)
        var pageable = PageRequest.of(0, 1000);
        List<ClientCardDTO> allCards = clientCardService.findAllWithAccessControl(pageable, loggedInUser).getContent();
        
        // Create ListDataProvider with the loaded data
        ListDataProvider<ClientCardDTO> dataProvider = new ListDataProvider<>(allCards);
        
        // Configure filters
        dataProvider.setFilter(card -> {
            MerchantDTO merchantValue = merchantFilter.getValue();
            String cardNumberValue = cardNumberFilter.getValue();
            
            // Merchant filter
            if (merchantValue != null && 
                (card.getClient() == null || card.getClient().getMerchantId() == null || 
                 !card.getClient().getMerchantId().equals(merchantValue.getId()))) {
                return false;
            }
            
            // Card number filter
            if (cardNumberValue != null && !cardNumberValue.isEmpty() && 
                (card.getMaskedPan() == null || !card.getMaskedPan().contains(cardNumberValue))) {
                return false;
            }
            
            return true;
        });
        
        grid.setDataProvider(dataProvider);
    }
}

