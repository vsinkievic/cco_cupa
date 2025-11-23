package lt.creditco.cupa.ui.clientcard;

import com.bpmid.vapp.base.ui.FormMode;
import com.bpmid.vapp.base.ui.MainLayout;
import com.github.f4b6a3.ulid.UlidCreator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.dto.ClientCardDTO;
import lt.creditco.cupa.service.dto.ClientDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Vaadin view for viewing and editing Client Card details.
 * Handles both view and edit modes in a single component.
 */
@Route(value = "client-cards", layout = MainLayout.class)
@PageTitle("Client Card Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class ClientCardDetailView extends VerticalLayout implements HasUrlParameter<String> {

    protected final ClientCardService clientCardService;
    protected final ClientService clientService;
    protected final CupaUserService cupaUserService;
    protected final CupaUser loggedInUser;
    
    protected final Binder<ClientCardDTO> binder = new BeanValidationBinder<>(ClientCardDTO.class);
    
    // Form fields
    private final TextField idField = new TextField("Card ID");
    private final TextField maskedPanField = new TextField("Masked PAN");
    private final TextField expiryDateField = new TextField("Expiry Date");
    private final TextField cardholderNameField = new TextField("Cardholder Name");
    private final ComboBox<ClientDTO> clientField = new ComboBox<>("Client");
    private final Checkbox isDefaultField = new Checkbox("Is Default");
    private final Checkbox isValidField = new Checkbox("Is Valid");
    
    // Buttons
    private final Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
    private final Button cancelButton = new Button("Cancel");
    private final Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
    private final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
    private final Button backButton = new Button("Back to List");
    
    private final H2 titleLabel = new H2();
    private final HorizontalLayout headerLayout = new HorizontalLayout();
    
    protected ClientCardDTO currentCard;
    private FormMode currentMode = FormMode.VIEW;
    
    public ClientCardDetailView(ClientCardService clientCardService, ClientService clientService, CupaUserService cupaUserService) {
        this.clientCardService = clientCardService;
        this.clientService = clientService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(headerLayout);
        add(createCardDetailsSection());
        add(createSystemInformationSection());
        
        configureFields();
        configureBinder();
        configureButtons();
        loadClients();
        
        updateHeader();
    }
    
    private void updateHeader() {
        headerLayout.removeAll();
        
        if (currentMode.isNew()) {
            titleLabel.setText("New Client Card");
        } else if (currentCard != null) {
            titleLabel.setText("Client Card: " + (currentCard.getMaskedPan() != null ? currentCard.getMaskedPan() : currentCard.getId()));
        } else {
            titleLabel.setText("Client Card Details");
        }
        
        headerLayout.add(titleLabel);
        headerLayout.add(saveButton, cancelButton, editButton, deleteButton, backButton);
        headerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.expand(titleLabel);
    }
    
    private VerticalLayout createCardDetailsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 sectionTitle = new H3("Card Details");
        section.add(sectionTitle);
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Set required indicators
        clientField.setRequiredIndicatorVisible(true);
        maskedPanField.setRequiredIndicatorVisible(true);
        maskedPanField.setHelperText("Format: XXXX-XXXX-XXXX-1234");
        
        // Field order: Client | Masked PAN, Cardholder Name | Expiry Date
        formLayout.add(clientField, maskedPanField);
        formLayout.add(cardholderNameField, expiryDateField);
        
        section.add(formLayout);
        return section;
    }
    
    private VerticalLayout createSystemInformationSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 sectionTitle = new H3("System Information");
        section.add(sectionTitle);
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // ID field always readonly
        idField.setReadOnly(true);
        
        // Gateway-managed fields always readonly
        isDefaultField.setReadOnly(true);
        isValidField.setReadOnly(true);
        
        formLayout.add(idField, 2); // Full width
        formLayout.add(isDefaultField, isValidField);
        
        section.add(formLayout);
        return section;
    }
    
    private void configureFields() {
        idField.setWidthFull();
        maskedPanField.setWidthFull();
        expiryDateField.setWidthFull();
        cardholderNameField.setWidthFull();
        clientField.setWidthFull();
    }
    
    private void loadClients() {
        Page<ClientDTO> clientsPage = clientService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser);
        var clients = clientsPage.getContent();
        clientField.setItems(clients);
        clientField.setItemLabelGenerator(client -> {
            String name = client.getName();
            return (name != null && !name.isEmpty()) ? name : client.getMerchantClientId();
        });
        
        // Auto-select if only one client available in NEW mode
        if (currentMode.isNew() && clients.size() == 1) {
            clientField.setValue(clients.get(0));
        }
    }
    
    private void configureBinder() {
        binder.forField(idField).bind(ClientCardDTO::getId, ClientCardDTO::setId);
        
        // Masked PAN is required
        binder.forField(maskedPanField)
            .asRequired("Masked PAN is required")
            .bind(ClientCardDTO::getMaskedPan, ClientCardDTO::setMaskedPan);
        
        binder.forField(expiryDateField).bind(ClientCardDTO::getExpiryDate, ClientCardDTO::setExpiryDate);
        binder.forField(cardholderNameField).bind(ClientCardDTO::getCardholderName, ClientCardDTO::setCardholderName);
        
        // Client is required
        binder.forField(clientField)
            .asRequired("Client is required")
            .bind(ClientCardDTO::getClient, ClientCardDTO::setClient);
        
        binder.forField(isDefaultField).bind(ClientCardDTO::getIsDefault, ClientCardDTO::setIsDefault);
        binder.forField(isValidField).bind(ClientCardDTO::getIsValid, ClientCardDTO::setIsValid);
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
    public void setParameter(BeforeEvent event, String cardId) {
        // Handle "new" mode (when cardId is null, the /new route was used)
        if (cardId == null) {
            log.debug("Creating new client card");
            ClientCardDTO newCard = new ClientCardDTO();
            // Set gateway-managed fields to null (will be set by gateway)
            newCard.setIsDefault(null);
            newCard.setIsValid(null);
            this.currentCard = newCard;
            binder.setBean(newCard);
            setFormMode(FormMode.NEW);
            updateHeader();
            return;
        }
        
        // Handle existing card (view mode)
        log.debug("Loading client card: {} for user: {}", cardId, loggedInUser.getLogin());
        ClientCardDTO card = clientCardService.findOneWithAccessControl(cardId, loggedInUser).orElse(null);
        if (card != null) {
            this.currentCard = card;
            binder.setBean(card);
            setFormMode(FormMode.VIEW); // Always start in view mode
            updateHeader();
        } else {
            Notification.show("Client card not found or access denied", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }
    
    protected void setFormMode(FormMode mode) {
        this.currentMode = mode;
        
        // ID field is always read-only
        idField.setReadOnly(true);
        
        // Gateway-managed fields are always readonly
        isDefaultField.setReadOnly(true);
        isValidField.setReadOnly(true);
        
        if (mode.isNew()) {
            // NEW mode: all fields editable except gateway fields
            clientField.setReadOnly(false);
            maskedPanField.setReadOnly(false);
            expiryDateField.setReadOnly(false);
            cardholderNameField.setReadOnly(false);
        } else {
            // VIEW/EDIT mode
            clientField.setReadOnly(true); // Cannot change card ownership
            maskedPanField.setReadOnly(true); // Card identity cannot change
            
            // These fields can be edited in EDIT mode
            expiryDateField.setReadOnly(!mode.isEditable());
            cardholderNameField.setReadOnly(!mode.isEditable());
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
            ClientCardDTO card = binder.getBean();
            
            try {
                if (currentMode.isNew()) {
                    // Generate ULID for new card if ID is not set
                    if (card.getId() == null || card.getId().isEmpty()) {
                        card.setId(UlidCreator.getUlid().toString());
                    }
                    clientCardService.save(card);
                    Notification.show("Client card created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Navigate to list after successful creation
                    navigateToList();
                } else {
                    clientCardService.update(card);
                    Notification.show("Client card updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Refresh to view mode
                    setFormMode(FormMode.VIEW);
                }
            } catch (Exception e) {
                log.error("Error saving client card", e);
                Notification.show("Error saving client card: " + e.getMessage())
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
            if (currentCard != null && currentCard.getId() != null) {
                ClientCardDTO card = clientCardService.findOneWithAccessControl(currentCard.getId(), loggedInUser).orElse(null);
                if (card != null) {
                    binder.setBean(card);
                    this.currentCard = card;
                }
            }
            setFormMode(FormMode.VIEW);
        }
    }
    
    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Client Card");
        dialog.setText("Are you sure you want to delete this card? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteCard());
        
        dialog.open();
    }
    
    private void deleteCard() {
        if (currentCard != null && currentCard.getId() != null) {
            try {
                clientCardService.delete(currentCard.getId());
                Notification.show("Client card deleted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigateToList();
            } catch (Exception e) {
                log.error("Error deleting client card", e);
                Notification.show("Error deleting client card: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
    
    protected void navigateToList() {
        getUI().ifPresent(ui -> ui.navigate(ClientCardListView.class));
    }
}

