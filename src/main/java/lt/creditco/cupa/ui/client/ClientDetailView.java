package lt.creditco.cupa.ui.client;

import com.bpmid.vapp.base.ui.FormMode;
import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
import com.github.f4b6a3.ulid.UlidCreator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.time.ZoneId;

/**
 * Vaadin view for viewing and editing Client details.
 * Handles both view and edit modes in a single component.
 */
@Route(value = "clients", layout = MainLayout.class)
@PageTitle("Client Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientDetailView extends VerticalLayout implements HasUrlParameter<String> {

    protected final ClientService clientService;
    protected final MerchantService merchantService;
    protected final CupaUserService cupaUserService;
    protected final CupaUser loggedInUser;
    
    protected final Binder<ClientDTO> binder = new BeanValidationBinder<>(ClientDTO.class);
    private final BreadcrumbBar breadcrumbBar = new BreadcrumbBar();
    
    // Section 1: Client Details (most used fields)
    private final ComboBox<MerchantDTO> merchantField = new ComboBox<>("Merchant");
    private final TextField merchantClientIdField = new TextField("Merchant Client ID");
    private final ComboBox<MerchantMode> environmentField = new ComboBox<>("Environment");
    private final TextField nameField = new TextField("Name");
    private final TextField emailAddressField = new TextField("Email");
    private final TextField mobileNumberField = new TextField("Mobile");
    private final TextField clientPhoneField = new TextField("Client Phone");
    
    // Section 2: Address fields
    private final TextField streetNameField = new TextField("Street Name");
    private final TextField streetNumberField = new TextField("Street Number");
    private final TextField streetSuffixField = new TextField("Street Suffix");
    private final TextField cityField = new TextField("City");
    private final TextField stateField = new TextField("State");
    private final TextField postCodeField = new TextField("Post Code");
    private final TextField countryField = new TextField("Country");
    
    // Section 3: System Information (all readonly)
    private final TextField idField = new TextField("Client ID");
    private final TextField gatewayClientIdField = new TextField("Gateway Client ID");
    private final Checkbox validField = new Checkbox("Valid");
    private final Checkbox isBlacklistedField = new Checkbox("Is Blacklisted");
    private final Checkbox isCorrelatedBlacklistedField = new Checkbox("Correlated Blacklisted");
    private final DateTimePicker createdInGatewayField = new DateTimePicker("Created in Gateway");
    private final DateTimePicker updatedInGatewayField = new DateTimePicker("Updated in Gateway");
    
    // Buttons
    private final Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
    private final Button cancelButton = new Button("Cancel");
    private final Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
    private final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
    private final Button backButton = new Button("Back to List");
    
    private final H2 titleLabel = new H2();
    private final HorizontalLayout headerLayout = new HorizontalLayout();
    
    protected ClientDTO currentClient;
    private FormMode currentMode = FormMode.VIEW;
    
    public ClientDetailView(ClientService clientService, MerchantService merchantService, CupaUserService cupaUserService) {
        this.clientService = clientService;
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(breadcrumbBar, headerLayout);
        add(createClientDetailsSection());
        add(createAddressSection());
        add(createSystemInformationSection());
        
        configureFields();
        configureBinder();
        configureButtons();
        loadMerchants();
        
        updateHeader();
    }
    
    private void updateHeader() {
        headerLayout.removeAll();
        
        if (currentMode.isNew()) {
            titleLabel.setText("New Client");
        } else if (currentClient != null) {
            titleLabel.setText("Client: " + (currentClient.getName() != null ? currentClient.getName() : currentClient.getId()));
        } else {
            titleLabel.setText("Client Details");
        }
        
        headerLayout.add(titleLabel);
        headerLayout.add(saveButton, cancelButton, editButton, deleteButton, backButton);
        headerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.expand(titleLabel);
    }
    
    private VerticalLayout createClientDetailsSection() {
        H3 sectionTitle = new H3("Client Details");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Set required indicators
        merchantField.setRequiredIndicatorVisible(true);
        merchantClientIdField.setRequiredIndicatorVisible(true);
        nameField.setRequiredIndicatorVisible(true);
        emailAddressField.setRequiredIndicatorVisible(true);
        mobileNumberField.setRequiredIndicatorVisible(true);
        
        // Set helper texts
        merchantClientIdField.setHelperText("Merchant's unique identifier");
        
        formLayout.add(merchantField, merchantClientIdField);
        formLayout.add(nameField, emailAddressField);
        formLayout.add(mobileNumberField, clientPhoneField);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createAddressSection() {
        H3 sectionTitle = new H3("Address");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(streetNameField, streetNumberField);
        formLayout.add(streetSuffixField, cityField);
        formLayout.add(stateField, postCodeField);
        formLayout.add(countryField, 2);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createSystemInformationSection() {
        H3 sectionTitle = new H3("System Information");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Gateway-managed fields are readonly
        idField.setReadOnly(true);
        gatewayClientIdField.setReadOnly(true);
        validField.setReadOnly(true);
        isBlacklistedField.setReadOnly(true);
        isCorrelatedBlacklistedField.setReadOnly(true);
        createdInGatewayField.setReadOnly(true);
        updatedInGatewayField.setReadOnly(true);
        
        formLayout.add(idField, gatewayClientIdField);
        formLayout.add(environmentField, validField);
        formLayout.add(isBlacklistedField, isCorrelatedBlacklistedField);
        formLayout.add(createdInGatewayField);
        formLayout.add(updatedInGatewayField);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private void configureFields() {
        idField.setWidthFull();
        merchantClientIdField.setWidthFull();
        gatewayClientIdField.setWidthFull();
        merchantField.setWidthFull();
        
        environmentField.setWidthFull();
        environmentField.setItems(MerchantMode.values());
        environmentField.setHelperText("Environment for this client");
        environmentField.setRequiredIndicatorVisible(true);
        
        nameField.setWidthFull();
        emailAddressField.setWidthFull();
        mobileNumberField.setWidthFull();
        clientPhoneField.setWidthFull();
    }
    
    private void loadMerchants() {
        var merchants = merchantService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser).getContent();
        merchantField.setItems(merchants);
        merchantField.setItemLabelGenerator(merchant -> merchant.getId() + " - " + merchant.getName());
        
        // Add value change listener to set default environment from merchant mode (only in NEW mode)
        merchantField.addValueChangeListener(e -> {
            MerchantDTO selectedMerchant = e.getValue();
            if (currentMode.isNew() && selectedMerchant != null && selectedMerchant.getMode() != null) {
                environmentField.setValue(selectedMerchant.getMode());
            }
        });
        
        // Auto-select merchant if only one is available in NEW mode
        if (currentMode.isNew() && merchants.size() == 1) {
            MerchantDTO singleMerchant = merchants.get(0);
            merchantField.setValue(singleMerchant);
            log.debug("Auto-selected single available merchant: {}", singleMerchant.getName());
        }
    }
    
    private void configureBinder() {
        // ID field
        binder.forField(idField).bind(ClientDTO::getId, ClientDTO::setId);
        
        // Merchant Client ID with validation (no spaces/whitespace)
        binder.forField(merchantClientIdField)
            .withValidator(id -> id != null && !id.trim().isEmpty(), "Merchant Client ID is required")
            .withValidator(id -> id != null && !id.contains(" ") && id.equals(id.trim()), 
                "Merchant Client ID must not contain spaces or whitespace characters")
            .bind(ClientDTO::getMerchantClientId, ClientDTO::setMerchantClientId);
        
        // Gateway Client ID - convert empty string to null to avoid unique constraint violation
        binder.forField(gatewayClientIdField)
            .withConverter(
                value -> (value == null || value.trim().isEmpty()) ? null : value,
                value -> value == null ? "" : value
            )
            .bind(ClientDTO::getGatewayClientId, ClientDTO::setGatewayClientId);
        
        // Merchant field binding (required)
        binder.forField(merchantField)
            .asRequired("Merchant is required")
            .bind(
                dto -> {
                    if (dto.getMerchantId() == null) return null;
                    return merchantField.getListDataView().getItems()
                        .filter(m -> dto.getMerchantId().equals(m.getId()))
                        .findFirst().orElse(null);
                },
                (dto, merchant) -> {
                    if (merchant != null) {
                        dto.setMerchantId(merchant.getId());
                        dto.setMerchantName(merchant.getName());
                    }
                }
            );
        
        // Environment field binding
        binder.forField(environmentField)
            .asRequired("Environment is required")
            .bind(ClientDTO::getEnvironment, ClientDTO::setEnvironment);
        
        // Required fields
        binder.forField(nameField)
            .asRequired("Name is required")
            .bind(ClientDTO::getName, ClientDTO::setName);
        
        binder.forField(emailAddressField)
            .asRequired("Email is required")
            .bind(ClientDTO::getEmailAddress, ClientDTO::setEmailAddress);
        
        binder.forField(mobileNumberField)
            .asRequired("Mobile is required")
            .bind(ClientDTO::getMobileNumber, ClientDTO::setMobileNumber);
        
        // Optional fields
        binder.forField(clientPhoneField).bind(ClientDTO::getClientPhone, ClientDTO::setClientPhone);
        
        // Address fields
        binder.forField(streetNameField).bind(ClientDTO::getStreetName, ClientDTO::setStreetName);
        binder.forField(streetNumberField).bind(ClientDTO::getStreetNumber, ClientDTO::setStreetNumber);
        binder.forField(streetSuffixField).bind(ClientDTO::getStreetSuffix, ClientDTO::setStreetSuffix);
        binder.forField(cityField).bind(ClientDTO::getCity, ClientDTO::setCity);
        binder.forField(stateField).bind(ClientDTO::getState, ClientDTO::setState);
        binder.forField(postCodeField).bind(ClientDTO::getPostCode, ClientDTO::setPostCode);
        binder.forField(countryField).bind(ClientDTO::getCountry, ClientDTO::setCountry);
        
        // Gateway-managed fields (readonly)
        binder.forField(validField).bind(ClientDTO::getValid, ClientDTO::setValid);
        binder.forField(isBlacklistedField).bind(ClientDTO::getIsBlacklisted, ClientDTO::setIsBlacklisted);
        binder.forField(isCorrelatedBlacklistedField).bind(ClientDTO::getIsCorrelatedBlacklisted, ClientDTO::setIsCorrelatedBlacklisted);
        
        // Gateway timestamp fields
        binder.forField(createdInGatewayField)
            .bind(
                dto -> dto.getCreatedInGateway() != null ? dto.getCreatedInGateway().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                (dto, value) -> dto.setCreatedInGateway(value != null ? value.atZone(ZoneId.systemDefault()).toInstant() : null)
            );
        
        binder.forField(updatedInGatewayField)
            .bind(
                dto -> dto.getUpdatedInGateway() != null ? dto.getUpdatedInGateway().atZone(ZoneId.systemDefault()).toLocalDateTime() : null,
                (dto, value) -> dto.setUpdatedInGateway(value != null ? value.atZone(ZoneId.systemDefault()).toInstant() : null)
            );
        
        binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));
    }
    
    private void configureButtons() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());
        
        cancelButton.addClickListener(e -> cancel());
        
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> setFormMode(FormMode.EDIT));
        
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());
        
        backButton.addClickListener(e -> navigateToList());
    }
    
    @Override
    public void setParameter(BeforeEvent event, String clientId) {
        // Handle "new" mode (when clientId is null, the /new route was used)
        if (clientId == null) {
            log.debug("Creating new client");
            ClientDTO newClient = new ClientDTO();
            // Gateway-managed fields: set to null (will be populated by gateway)
            newClient.setValid(null);
            newClient.setIsBlacklisted(null);
            newClient.setIsCorrelatedBlacklisted(null);
            newClient.setGatewayClientId(null);
            this.currentClient = newClient;
            binder.setBean(newClient);
            setFormMode(FormMode.NEW);
            updateHeader();
            
            // Update breadcrumb for NEW mode
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Clients", ClientListView.class)
                    .currentLink("New", ClientNewRouteHandler.class)
                    .build()
            );
            return;
        }
        
        // Handle existing client (view mode)
        log.debug("Loading client: {} for user: {}", clientId, loggedInUser.getLogin());
        ClientDTO client = clientService.findOneWithAccessControl(clientId, loggedInUser).orElse(null);
        if (client != null) {
            this.currentClient = client;
            binder.setBean(client);
            setFormMode(FormMode.VIEW); // Always start in view mode
            updateHeader();
            
            // Update breadcrumb for existing client
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Clients", ClientListView.class)
                    .currentLink(client.getMerchantClientId() != null ? client.getMerchantClientId() : client.getId(),
                                 ClientDetailView.class, clientId)
                    .build()
            );
        } else {
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Clients", ClientListView.class)
                    .current("Not Found")
                    .build()
            );
            Notification.show("Client not found or access denied", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }
    
    protected void setFormMode(FormMode mode) {
        this.currentMode = mode;
        binder.setReadOnly(!mode.isEditable());
        
        // System Information fields - always readonly
        idField.setReadOnly(true);
        gatewayClientIdField.setReadOnly(true);
        validField.setReadOnly(true);
        isBlacklistedField.setReadOnly(true);
        isCorrelatedBlacklistedField.setReadOnly(true);
        createdInGatewayField.setReadOnly(true);
        updatedInGatewayField.setReadOnly(true);
        
        // Merchant: editable only in NEW mode, readonly in EDIT mode
        merchantField.setReadOnly(!mode.isNew());
        
        // Merchant Client ID: editable only in NEW mode, readonly in EDIT mode
        merchantClientIdField.setReadOnly(!mode.isNew());
        
        // Environment: editable in edit mode (not a gateway-managed field)
        environmentField.setReadOnly(!mode.isEditable());
        
        // In NEW mode: hide all system information fields except Environment
        if (mode.isNew()) {
            idField.setVisible(false);
            gatewayClientIdField.setVisible(false);
            validField.setVisible(false);
            isBlacklistedField.setVisible(false);
            isCorrelatedBlacklistedField.setVisible(false);
            createdInGatewayField.setVisible(false);
            updatedInGatewayField.setVisible(false);
            environmentField.setVisible(true);
        } else {
            // In EDIT/VIEW mode: show all system information fields
            idField.setVisible(true);
            gatewayClientIdField.setVisible(true);
            validField.setVisible(true);
            isBlacklistedField.setVisible(true);
            isCorrelatedBlacklistedField.setVisible(true);
            createdInGatewayField.setVisible(true);
            updatedInGatewayField.setVisible(true);
            environmentField.setVisible(true);
        }
        
        // Toggle button visibility using FormMode helper methods
        saveButton.setVisible(mode.isSaveButtonVisible());
        cancelButton.setVisible(mode.isCancelButtonVisible());
        editButton.setVisible(mode.isEditButtonVisible());
        deleteButton.setVisible(mode.isDeleteButtonVisible());
        backButton.setVisible(mode.isBackToListButtonVisible());
        
        // Initialize save button enabled state based on binder validation
        saveButton.setEnabled(binder.isValid());
    }
    
    private void save() {
        if (binder.validate().isOk()) {
            ClientDTO client = binder.getBean();
            
            try {
                if (currentMode.isNew()) {
                    // Generate ULID for new client
                    if (client.getId() == null || client.getId().isEmpty()) {
                        client.setId(UlidCreator.getUlid().toString());
                    }
                    clientService.save(client);
                    Notification.show("Client created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Navigate to list after successful creation (per design rules)
                    navigateToList();
                } else {
                    clientService.update(client);
                    Notification.show("Client updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Refresh to view mode
                    setFormMode(FormMode.VIEW);
                }
            } catch (Exception e) {
                log.error("Error saving client", e);
                Notification.show("Error saving client: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void cancel() {
        if (currentMode.isNew()) {
            navigateToList();
        } else {
            // Reload the current entity to discard changes
            if (currentClient != null && currentClient.getId() != null) {
                ClientDTO client = clientService.findOneWithAccessControl(currentClient.getId(), loggedInUser).orElse(null);
                if (client != null) {
                    binder.setBean(client);
                    this.currentClient = client;
                }
            }
            setFormMode(FormMode.VIEW);
        }
    }
    
    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Client");
        dialog.setText("Are you sure you want to delete this client? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteClient());
        
        dialog.open();
    }
    
    private void deleteClient() {
        if (currentClient != null && currentClient.getId() != null) {
            try {
                clientService.delete(currentClient.getId());
                Notification.show("Client deleted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigateToList();
            } catch (Exception e) {
                log.error("Error deleting client", e);
                Notification.show("Error deleting client: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
    
    protected void navigateToList() {
        getUI().ifPresent(ui -> ui.navigate(ClientListView.class));
    }
}

