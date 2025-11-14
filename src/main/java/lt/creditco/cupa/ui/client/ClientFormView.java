package lt.creditco.cupa.ui.client;

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
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * Vaadin view for creating/editing a Client.
 */
@Route(value = "clients/edit", layout = MainLayout.class)
@PageTitle("Edit Client | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final ClientService clientService;
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Binder<ClientDTO> binder = new BeanValidationBinder<>(ClientDTO.class);
    
    private String clientId;
    
    // Basic fields
    private final TextField idField = new TextField("Client ID");
    private final TextField merchantClientIdField = new TextField("Merchant Client ID");
    private final TextField gatewayClientIdField = new TextField("Gateway Client ID");
    private final ComboBox<MerchantDTO> merchantField = new ComboBox<>("Merchant");
    private final TextField nameField = new TextField("Name");
    private final TextField emailAddressField = new TextField("Email");
    private final TextField mobileNumberField = new TextField("Mobile");
    private final TextField clientPhoneField = new TextField("Phone");
    private final Checkbox validField = new Checkbox("Valid");
    private final Checkbox isBlacklistedField = new Checkbox("Is Blacklisted");
    
    // Address fields
    private final TextField streetNumberField = new TextField("Street Number");
    private final TextField streetNameField = new TextField("Street Name");
    private final TextField streetSuffixField = new TextField("Street Suffix");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField postCodeField = new TextField("Post Code");
    private final TextField countryField = new TextField("Country");
    
    public ClientFormView(ClientService clientService, MerchantService merchantService, CupaUserService cupaUserService) {
        this.clientService = clientService;
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Client Details");
        add(title);
        
        add(createFormLayout());
        add(createButtonBar());
        
        configureFields();
        configureBinder();
        loadMerchants();
    }
    
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        idField.setReadOnly(true);
        gatewayClientIdField.setReadOnly(true);
        
        merchantClientIdField.setRequiredIndicatorVisible(true);
        nameField.setRequiredIndicatorVisible(true);
        
        formLayout.add(idField, merchantClientIdField);
        formLayout.add(gatewayClientIdField, merchantField);
        formLayout.add(nameField, emailAddressField);
        formLayout.add(mobileNumberField, clientPhoneField);
        formLayout.add(validField, isBlacklistedField);
        formLayout.add(streetNumberField, streetNameField);
        formLayout.add(streetSuffixField, cityField);
        formLayout.add(stateField, postCodeField);
        formLayout.add(countryField, 2);
        
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
        merchantClientIdField.setWidthFull();
        gatewayClientIdField.setWidthFull();
        merchantField.setWidthFull();
        nameField.setWidthFull();
        emailAddressField.setWidthFull();
        mobileNumberField.setWidthFull();
        clientPhoneField.setWidthFull();
    }
    
    private void loadMerchants() {
        var merchants = merchantService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser).getContent();
        merchantField.setItems(merchants);
        merchantField.setItemLabelGenerator(MerchantDTO::getName);
    }
    
    private void configureBinder() {
        binder.forField(idField).bind(ClientDTO::getId, ClientDTO::setId);
        binder.forField(merchantClientIdField).bind(ClientDTO::getMerchantClientId, ClientDTO::setMerchantClientId);
        binder.forField(gatewayClientIdField).bind(ClientDTO::getGatewayClientId, ClientDTO::setGatewayClientId);
//        binder.forField(merchantField).bind(ClientDTO::getMerchantName, ClientDTO::setMerchantName);
        binder.forField(nameField).bind(ClientDTO::getName, ClientDTO::setName);
        binder.forField(emailAddressField).bind(ClientDTO::getEmailAddress, ClientDTO::setEmailAddress);
        binder.forField(mobileNumberField).bind(ClientDTO::getMobileNumber, ClientDTO::setMobileNumber);
        binder.forField(clientPhoneField).bind(ClientDTO::getClientPhone, ClientDTO::setClientPhone);
        binder.forField(validField).bind(ClientDTO::getValid, ClientDTO::setValid);
        binder.forField(isBlacklistedField).bind(ClientDTO::getIsBlacklisted, ClientDTO::setIsBlacklisted);
        binder.forField(streetNumberField).bind(ClientDTO::getStreetNumber, ClientDTO::setStreetNumber);
        binder.forField(streetNameField).bind(ClientDTO::getStreetName, ClientDTO::setStreetName);
        binder.forField(streetSuffixField).bind(ClientDTO::getStreetSuffix, ClientDTO::setStreetSuffix);
        binder.forField(cityField).bind(ClientDTO::getCity, ClientDTO::setCity);
        binder.forField(stateField).bind(ClientDTO::getState, ClientDTO::setState);
        binder.forField(postCodeField).bind(ClientDTO::getPostCode, ClientDTO::setPostCode);
        binder.forField(countryField).bind(ClientDTO::getCountry, ClientDTO::setCountry);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.equals("new")) {
            clientId = parameter;
            loadClient(clientId);
        } else {
            // New client
            ClientDTO newClient = new ClientDTO();
            newClient.setValid(true);
            newClient.setIsBlacklisted(false);
            binder.setBean(newClient);
        }
    }
    
    private void loadClient(String id) {
        log.debug("Loading client: {} for user: {}", id, loggedInUser.getLogin());
        ClientDTO client = clientService.findOneWithAccessControl(id, loggedInUser).orElse(null);
        if (client != null) {
            binder.setBean(client);
        } else {
            Notification.show("Client not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cancel();
        }
    }
    
    private void save() {
        if (binder.validate().isOk()) {
            ClientDTO client = binder.getBean();
            
            try {
                if (client.getId() == null || client.getId().isEmpty()) {
                    client.setId(UUID.randomUUID().toString());
                    clientService.save(client);
                    Notification.show("Client created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    clientService.update(client);
                    Notification.show("Client updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                
                getUI().ifPresent(ui -> ui.navigate(ClientListView.class));
            } catch (Exception e) {
                Notification.show("Error saving client: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void cancel() {
        getUI().ifPresent(ui -> ui.navigate(ClientListView.class));
    }
}

