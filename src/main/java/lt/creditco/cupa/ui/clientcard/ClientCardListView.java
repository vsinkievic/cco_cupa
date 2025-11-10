package lt.creditco.cupa.ui.clientcard;

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
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.springframework.data.domain.PageRequest;

/**
 * Vaadin view for listing Client Cards.
 */
@Route(value = "client-cards", layout = MainLayout.class)
@PageTitle("Client Cards | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientCardListView extends VerticalLayout {

    private final ClientCardService clientCardService;
    private final MerchantService merchantService;
    
    private final Grid<ClientCardDTO> grid = new Grid<>(ClientCardDTO.class, false);
    
    private final ComboBox<MerchantDTO> merchantFilter = new ComboBox<>("Merchant");
    private final TextField cardNumberFilter = new TextField("Card Number");
    
    public ClientCardListView(ClientCardService clientCardService, MerchantService merchantService) {
        this.clientCardService = clientCardService;
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
        
        cardNumberFilter.setPlaceholder("Filter by card number");
        cardNumberFilter.setClearButtonVisible(true);
        cardNumberFilter.setValueChangeMode(ValueChangeMode.LAZY);
        cardNumberFilter.addValueChangeListener(e -> refreshGrid());
        
        // Create button
        Button createButton = new Button("New Card", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ClientCardFormView.class, "new")));
        
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
        
        // Action buttons
        grid.addComponentColumn(card -> {
            Button viewButton = new Button("View", VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewButton.addClickListener(e -> 
                getUI().ifPresent(ui -> ui.navigate(ClientCardDetailView.class, card.getId()))
            );
            
            Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editButton.addClickListener(e -> 
                getUI().ifPresent(ui -> ui.navigate(ClientCardFormView.class, card.getId()))
            );
            
            return new HorizontalLayout(viewButton, editButton);
        }).setHeader("Actions").setAutoWidth(true);
        
        grid.setSizeFull();
        
        return grid;
    }
    
    private void refreshGrid() {
        MerchantDTO merchantValue = merchantFilter.getValue();
        String cardNumberValue = cardNumberFilter.getValue();
        
        CallbackDataProvider<ClientCardDTO, Void> dataProvider = DataProvider.fromCallbacks(
            query -> {
                int page = query.getPage();
                int size = query.getPageSize();
                
                var pageable = PageRequest.of(page, size);
                var result = clientCardService.findAll(pageable);
                
                // Apply filters manually
                return result.stream()
                    .filter(c -> merchantValue == null || 
                                (c.getClient() != null && c.getClient().getMerchantId() != null && 
                                 c.getClient().getMerchantId().equals(merchantValue.getId())))
                    .filter(c -> cardNumberValue == null || cardNumberValue.isEmpty() || 
                                (c.getMaskedPan() != null && c.getMaskedPan().contains(cardNumberValue)));
            },
            query -> {
                return (int) clientCardService.count();
            }
        );
        
        grid.setDataProvider(dataProvider);
    }
}

