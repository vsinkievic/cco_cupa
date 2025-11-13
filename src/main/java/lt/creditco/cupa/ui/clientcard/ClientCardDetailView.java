package lt.creditco.cupa.ui.clientcard;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.dto.ClientCardDTO;

/**
 * Vaadin view for viewing Client Card details.
 */
@Route(value = "client-cards/view", layout = MainLayout.class)
@PageTitle("Client Card Details | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientCardDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final ClientCardService clientCardService;
    
    private ClientCardDTO card;
    
    private final VerticalLayout contentLayout = new VerticalLayout();
    
    public ClientCardDetailView(ClientCardService clientCardService) {
        this.clientCardService = clientCardService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(contentLayout);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String cardId) {
        ClientCardDTO card = clientCardService.findOne(cardId).orElse(null);
        if (card != null) {
            this.card = card;
            buildContent();
        } else {
            Notification.show("Client card not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(ClientCardListView.class));
        }
    }
    
    private void buildContent() {
        contentLayout.removeAll();
        
        H2 title = new H2("Client Card Details");
        
        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(ClientCardFormView.class, card.getId()))
        );
        
        Button backButton = new Button("Back to List");
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(ClientCardListView.class))
        );
        
        HorizontalLayout header = new HorizontalLayout(title, editButton, backButton);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setWidthFull();
        header.expand(title);
        
        Div content = new Div();
        content.add(createField("Card ID", card.getId()));
        content.add(createField("Masked PAN", card.getMaskedPan()));
        content.add(createField("Expiry Date", card.getExpiryDate()));
        content.add(createField("Cardholder Name", card.getCardholderName()));
        content.add(createField("Client", card.getClient() != null ? card.getClient().getName() : ""));
        content.add(createField("Merchant", ""));
        content.add(createField("Is Default", card.getIsDefault() != null ? card.getIsDefault().toString() : ""));
        content.add(createField("Is Valid", card.getIsValid() != null ? card.getIsValid().toString() : ""));
        
        contentLayout.add(header, content);
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

