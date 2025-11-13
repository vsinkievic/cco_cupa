package lt.creditco.cupa.ui.client;

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
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.dto.ClientDTO;

/**
 * Vaadin view for viewing Client details.
 */
@Route(value = "clients/view", layout = MainLayout.class)
@PageTitle("Client Details | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final ClientService clientService;
    
    private ClientDTO client;
    
    private final VerticalLayout contentLayout = new VerticalLayout();
    
    public ClientDetailView(ClientService clientService) {
        this.clientService = clientService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(contentLayout);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String clientId) {
        ClientDTO client = clientService.findOne(clientId).orElse(null);
        if (client != null) {
            this.client = client;
            buildContent();
        } else {
            Notification.show("Client not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(ClientListView.class));   
        }
    }
    
    private void buildContent() {
        contentLayout.removeAll();
        
        H2 title = new H2("Client: " + client.getName());
        
        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(ClientFormView.class, client.getId()))
        );
        
        Button backButton = new Button("Back to List");
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(ClientListView.class))
        );
        
        HorizontalLayout header = new HorizontalLayout(title, editButton, backButton);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setWidthFull();
        header.expand(title);
        
        Div content = new Div();
        content.add(createField("Client ID", client.getId()));
        content.add(createField("Merchant Client ID", client.getMerchantClientId()));
        content.add(createField("Gateway Client ID", client.getGatewayClientId()));
        content.add(createField("Merchant", client.getMerchantName()));
        content.add(createField("Name", client.getName()));
        content.add(createField("Email", client.getEmailAddress()));
        content.add(createField("Mobile", client.getMobileNumber()));
        content.add(createField("Phone", client.getClientPhone()));
        content.add(createField("Valid", client.getValid() != null ? client.getValid().toString() : ""));
        content.add(createField("Is Blacklisted", client.getIsBlacklisted() != null ? client.getIsBlacklisted().toString() : ""));
        content.add(createField("Street Number", client.getStreetNumber()));
        content.add(createField("Street Name", client.getStreetName()));
        content.add(createField("Street Suffix", client.getStreetSuffix()));
        content.add(createField("City", client.getCity()));
        content.add(createField("State", client.getState()));
        content.add(createField("Post Code", client.getPostCode()));
        content.add(createField("Country", client.getCountry()));
        
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

