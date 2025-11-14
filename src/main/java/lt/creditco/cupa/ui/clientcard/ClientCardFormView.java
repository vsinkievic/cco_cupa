package lt.creditco.cupa.ui.clientcard;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.dto.ClientDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Vaadin view for creating/editing a Client Card.
 */
@Route(value = "client-cards/edit", layout = MainLayout.class)
@PageTitle("Edit Client Card | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientCardFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final ClientCardService clientCardService;
    private final ClientService clientService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Binder<ClientCardDTO> binder = new BeanValidationBinder<>(ClientCardDTO.class);
    
    private String cardId;
    
    private final TextField idField = new TextField("Card ID");
    private final TextField maskedPanField = new TextField("Masked PAN");
    private final TextField expiryDateField = new TextField("Expiry Date");
    private final TextField cardholderNameField = new TextField("Cardholder Name");
    private final ComboBox<ClientDTO> clientField = new ComboBox<>("Client");
    private final Checkbox isDefaultField = new Checkbox("Is Default");
    private final Checkbox isValidField = new Checkbox("Is Valid");
    
    public ClientCardFormView(ClientCardService clientCardService, ClientService clientService, CupaUserService cupaUserService) {
        this.clientCardService = clientCardService;
        this.clientService = clientService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Client Card Details");
        add(title);
        
        add(createFormLayout());
        add(createButtonBar());
        
        configureFields();
        configureBinder();
        loadClients();
    }
    
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        idField.setReadOnly(true);
        maskedPanField.setRequiredIndicatorVisible(true);
        maskedPanField.setHelperText("Format: XXXX-XXXX-XXXX-1234");
        
        formLayout.add(idField, clientField);
        formLayout.add(maskedPanField, expiryDateField);
        formLayout.add(cardholderNameField, 2);
        formLayout.add(isDefaultField, isValidField);
        
        return formLayout;
    }
    
    private HorizontalLayout createButtonBar() {
        Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> cancel());
        
        HorizontalLayout buttonBar = new HorizontalLayout(saveButton, cancelButton);
        buttonBar.setSpacing(true);
        
        return buttonBar;
    }
    
    private void configureFields() {
        idField.setWidthFull();
        maskedPanField.setWidthFull();
        expiryDateField.setWidthFull();
        cardholderNameField.setWidthFull();
        clientField.setWidthFull();
    }
    
    private void loadClients() {
        var clients = clientService.findAll(PageRequest.of(0, 100)).getContent();
        clientField.setItems(clients);
        clientField.setItemLabelGenerator(ClientDTO::getName);
    }
    
    private void configureBinder() {
        binder.forField(idField).bind(ClientCardDTO::getId, ClientCardDTO::setId);
        binder.forField(maskedPanField).bind(ClientCardDTO::getMaskedPan, ClientCardDTO::setMaskedPan);
        binder.forField(expiryDateField).bind(ClientCardDTO::getExpiryDate, ClientCardDTO::setExpiryDate);
        binder.forField(cardholderNameField).bind(ClientCardDTO::getCardholderName, ClientCardDTO::setCardholderName);
        binder.forField(clientField).bind(ClientCardDTO::getClient, ClientCardDTO::setClient);
        binder.forField(isDefaultField).bind(ClientCardDTO::getIsDefault, ClientCardDTO::setIsDefault);
        binder.forField(isValidField).bind(ClientCardDTO::getIsValid, ClientCardDTO::setIsValid);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.equals("new")) {
            cardId = parameter;
            loadCard(cardId);
        } else {
            // New card
            ClientCardDTO newCard = new ClientCardDTO();
            newCard.setIsDefault(false);
            newCard.setIsValid(true);
            binder.setBean(newCard);
        }
    }
    
    private void loadCard(String id) {
        log.debug("Loading client card: {} for user: {}", id, loggedInUser.getLogin());
        ClientCardDTO card = clientCardService.findOneWithAccessControl(id, loggedInUser).orElse(null);
        if (card != null) {
            binder.setBean(card);
        } else {
            Notification.show("Client card not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cancel();
        }
    }
    
    private void save() {
        if (binder.validate().isOk()) {
            ClientCardDTO card = binder.getBean();
            
            try {
                if (card.getId() == null || card.getId().isEmpty()) {
                    card.setId(UUID.randomUUID().toString());
                    clientCardService.save(card);
                    Notification.show("Client card created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    clientCardService.update(card);
                    Notification.show("Client card updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                
                getUI().ifPresent(ui -> ui.navigate(ClientCardListView.class));
            } catch (Exception e) {
                Notification.show("Error saving client card: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void cancel() {
        getUI().ifPresent(ui -> ui.navigate(ClientCardListView.class));
    }
}

