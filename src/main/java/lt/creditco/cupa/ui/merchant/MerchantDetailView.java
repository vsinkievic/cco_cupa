package lt.creditco.cupa.ui.merchant;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.MerchantDTO;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

/**
 * Vaadin view for viewing and editing Merchant details.
 * Handles both view and edit modes in a single component.
 */
//@Route(value = "merchants/new", layout = MainLayout.class)
@Route(value = "merchants", layout = MainLayout.class)
//@RouteAlias(value = "merchants/new", layout = MainLayout.class)
//@PageTitle("Merchant Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
@Slf4j
public class MerchantDetailView extends VerticalLayout implements HasUrlParameter<String> {

    protected final MerchantService merchantService;
    protected final CupaUserService cupaUserService;
    protected final CupaUser loggedInUser;
    
    protected final Binder<MerchantDTO> binder = new BeanValidationBinder<>(MerchantDTO.class);
    
    // Basic fields
    private final TextField idField = new TextField("Merchant ID");
    private final TextField nameField = new TextField("Name");
    private final ComboBox<MerchantMode> modeField = new ComboBox<>("Mode");
    private final ComboBox<MerchantStatus> statusField = new ComboBox<>("Status");
    private final BigDecimalField balanceField = new BigDecimalField("Balance");
    private final ComboBox<Currency> currencyField = new ComboBox<>("Currency");
    
    // TEST environment fields
    private final TextField cupaTestApiKeyField = new TextField("CUPA Test API Key");
    private final TextField remoteTestUrlField = new TextField("Remote Test URL");
    private final TextField remoteTestMerchantIdField = new TextField("Remote Test Merchant ID");
    private final TextField remoteTestMerchantKeyField = new TextField("Remote Test Merchant Key");
    private final TextField remoteTestApiKeyField = new TextField("Remote Test API Key");
    
    // PROD environment fields
    private final TextField cupaProdApiKeyField = new TextField("CUPA Production API Key");
    private final TextField remoteProdUrlField = new TextField("Remote Production URL");
    private final TextField remoteProdMerchantIdField = new TextField("Remote Production Merchant ID");
    private final TextField remoteProdMerchantKeyField = new TextField("Remote Production Merchant Key");
    private final TextField remoteProdApiKeyField = new TextField("Remote Production API Key");
    
    // Buttons
    private final Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
    private final Button cancelButton = new Button("Cancel");
    private final Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
    private final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
    private final Button backButton = new Button("Back to List");
    
    // API Key generation buttons (only visible in edit mode)
    private final Button generateTestKeyButton = new Button("Generate", VaadinIcon.REFRESH.create());
    private final Button generateProdKeyButton = new Button("Generate", VaadinIcon.REFRESH.create());
    
    private final H2 titleLabel = new H2();
    private final HorizontalLayout headerLayout = new HorizontalLayout();
    
    protected MerchantDTO currentMerchant;
    protected boolean isNewMode = false;
    
    public MerchantDetailView(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(headerLayout);
        add(createBasicInfoSection());
        add(createEnvironmentCredentialsSection());
        
        configureFields();
        configureBinder();
        configureButtons();
        
        updateHeader();
    }

    public void newMerchant() {
        this.isNewMode = true;
        MerchantDTO newMerchant = new MerchantDTO();
        newMerchant.setMode(MerchantMode.TEST);
        newMerchant.setStatus(MerchantStatus.ACTIVE);
        newMerchant.setBalance(java.math.BigDecimal.ZERO);
        newMerchant.setCurrency(Currency.USD);
        this.currentMerchant = newMerchant;
        binder.setBean(newMerchant);
        setEditMode(true);
    }
    
    private void updateHeader() {
        headerLayout.removeAll();
        
        if (isNewMode) {
            titleLabel.setText("New Merchant");
        } else if (currentMerchant != null) {
            titleLabel.setText("Merchant: " + (currentMerchant.getName() != null ? currentMerchant.getName() : currentMerchant.getId()));
        } else {
            titleLabel.setText("Merchant Details");
        }
        
        headerLayout.add(titleLabel);
        headerLayout.add(saveButton, cancelButton, editButton, deleteButton, backButton);
        headerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.expand(titleLabel);
    }
    
    private VerticalLayout createBasicInfoSection() {
        H3 sectionTitle = new H3("Basic Information");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        idField.setRequiredIndicatorVisible(true);
        idField.setHelperText("Human-readable configuration code (no spaces allowed)");
        
        nameField.setRequiredIndicatorVisible(true);
        modeField.setRequiredIndicatorVisible(true);
        statusField.setRequiredIndicatorVisible(true);
        
        balanceField.setHelperText("System-managed, not editable");
        
        formLayout.add(idField, nameField, modeField, statusField, balanceField, currencyField);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private HorizontalLayout createEnvironmentCredentialsSection() {
        // Create TEST environment section
        H3 testSectionTitle = new H3("TEST Environment Credentials");
        
        FormLayout testFormLayout = new FormLayout();
        testFormLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1)
        );
        
        generateTestKeyButton.addClickListener(e -> {
            cupaTestApiKeyField.setValue(UUID.randomUUID().toString());
            Notification.show("New API key generated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        cupaTestApiKeyField.setSuffixComponent(generateTestKeyButton);
        
        testFormLayout.add(cupaTestApiKeyField);
        testFormLayout.add(remoteTestUrlField);
        testFormLayout.add(remoteTestMerchantIdField);
        testFormLayout.add(remoteTestMerchantKeyField);
        testFormLayout.add(remoteTestApiKeyField);
        
        VerticalLayout testSection = new VerticalLayout(testSectionTitle, testFormLayout);
        testSection.setPadding(false);
        testSection.setSpacing(false);
        
        // Create PRODUCTION environment section
        H3 prodSectionTitle = new H3("PRODUCTION Environment Credentials");
        
        FormLayout prodFormLayout = new FormLayout();
        prodFormLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1)
        );
        
        generateProdKeyButton.addClickListener(e -> {
            cupaProdApiKeyField.setValue(UUID.randomUUID().toString());
            Notification.show("New API key generated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        cupaProdApiKeyField.setSuffixComponent(generateProdKeyButton);
        
        prodFormLayout.add(cupaProdApiKeyField);
        prodFormLayout.add(remoteProdUrlField);
        prodFormLayout.add(remoteProdMerchantIdField);
        prodFormLayout.add(remoteProdMerchantKeyField);
        prodFormLayout.add(remoteProdApiKeyField);
        
        VerticalLayout prodSection = new VerticalLayout(prodSectionTitle, prodFormLayout);
        prodSection.setPadding(false);
        prodSection.setSpacing(false);
        
        // Create horizontal layout to hold both sections side by side
        HorizontalLayout horizontalLayout = new HorizontalLayout(testSection, prodSection);
        horizontalLayout.setWidthFull();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setPadding(false);
        horizontalLayout.expand(testSection, prodSection);
        
        return horizontalLayout;
    }
    
    private void configureFields() {
        modeField.setItems(MerchantMode.values());
        statusField.setItems(MerchantStatus.values());
        currencyField.setItems(Currency.values());
        
        // Make all text fields full width
        idField.setWidthFull();
        nameField.setWidthFull();
        cupaTestApiKeyField.setWidthFull();
        remoteTestUrlField.setWidthFull();
        remoteTestMerchantIdField.setWidthFull();
        remoteTestMerchantKeyField.setWidthFull();
        remoteTestApiKeyField.setWidthFull();
        cupaProdApiKeyField.setWidthFull();
        remoteProdUrlField.setWidthFull();
        remoteProdMerchantIdField.setWidthFull();
        remoteProdMerchantKeyField.setWidthFull();
        remoteProdApiKeyField.setWidthFull();
    }
    
    private void configureBinder() {
        // Bind basic fields with validation
        binder.forField(idField)
            .withValidator(id -> id != null && !id.trim().isEmpty(), "ID is required")
            .withValidator(id -> id != null && !id.contains(" ") && id.equals(id.trim()), 
                "ID must not contain spaces or whitespace characters")
            .bind(MerchantDTO::getId, MerchantDTO::setId);
        binder.forField(nameField).bind(MerchantDTO::getName, MerchantDTO::setName);
        binder.forField(modeField).bind(MerchantDTO::getMode, MerchantDTO::setMode);
        binder.forField(statusField).bind(MerchantDTO::getStatus, MerchantDTO::setStatus);
        binder.forField(balanceField).bind(MerchantDTO::getBalance, MerchantDTO::setBalance);
        binder.forField(currencyField).bind(MerchantDTO::getCurrency, MerchantDTO::setCurrency);
        
        // TEST environment fields
        binder.forField(cupaTestApiKeyField).bind(MerchantDTO::getCupaTestApiKey, MerchantDTO::setCupaTestApiKey);
        binder.forField(remoteTestUrlField).bind(MerchantDTO::getRemoteTestUrl, MerchantDTO::setRemoteTestUrl);
        binder.forField(remoteTestMerchantIdField).bind(MerchantDTO::getRemoteTestMerchantId, MerchantDTO::setRemoteTestMerchantId);
        binder.forField(remoteTestMerchantKeyField).bind(MerchantDTO::getRemoteTestMerchantKey, MerchantDTO::setRemoteTestMerchantKey);
        binder.forField(remoteTestApiKeyField).bind(MerchantDTO::getRemoteTestApiKey, MerchantDTO::setRemoteTestApiKey);
        
        // PROD environment fields
        binder.forField(cupaProdApiKeyField).bind(MerchantDTO::getCupaProdApiKey, MerchantDTO::setCupaProdApiKey);
        binder.forField(remoteProdUrlField).bind(MerchantDTO::getRemoteProdUrl, MerchantDTO::setRemoteProdUrl);
        binder.forField(remoteProdMerchantIdField).bind(MerchantDTO::getRemoteProdMerchantId, MerchantDTO::setRemoteProdMerchantId);
        binder.forField(remoteProdMerchantKeyField).bind(MerchantDTO::getRemoteProdMerchantKey, MerchantDTO::setRemoteProdMerchantKey);
        binder.forField(remoteProdApiKeyField).bind(MerchantDTO::getRemoteProdApiKey, MerchantDTO::setRemoteProdApiKey);
        
        binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));
    }
    
    private void configureButtons() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());
        
        cancelButton.addClickListener(e -> cancel());
        
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> setEditMode(true));
        
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());
        
        backButton.addClickListener(e -> navigateToList());
    }
    
    @Override
    public void setParameter(BeforeEvent event, String merchantId) {
        // Handle "new" mode (when merchantId is null, the /new route was used)
        if (merchantId == null) {
            log.debug("Creating new merchant");
            this.isNewMode = true;
            MerchantDTO newMerchant = new MerchantDTO();
            newMerchant.setMode(MerchantMode.TEST);
            newMerchant.setStatus(MerchantStatus.ACTIVE);
            newMerchant.setBalance(java.math.BigDecimal.ZERO);
            newMerchant.setCurrency(Currency.USD);
            this.currentMerchant = newMerchant;
            binder.setBean(newMerchant);
            setEditMode(true);
            updateHeader();
            return;
        }
        
        // Handle existing merchant (view mode)
        log.debug("Loading merchant: {} for user: {}", merchantId, loggedInUser.getLogin());
        MerchantDTO merchant = merchantService.findOneWithAccessControl(merchantId, loggedInUser).orElse(null);
        if (merchant != null) {
            this.currentMerchant = merchant;
            this.isNewMode = false;
            binder.setBean(merchant);
            setEditMode(false); // Always start in view mode
            updateHeader();
        } else {
            Notification.show("Merchant not found or access denied", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }
    
    protected void setEditMode(boolean edit) {
        binder.setReadOnly(!edit);
        
        // ID field: editable only in new mode, readonly in edit/view modes
        idField.setReadOnly(!isNewMode);
        
        // Balance field: always readonly (system managed)
        balanceField.setReadOnly(true);
        
        // Currency field: editable in both create and edit modes
        currencyField.setReadOnly(false);
        
        // Show/hide generate buttons
        generateTestKeyButton.setVisible(edit);
        generateProdKeyButton.setVisible(edit);
        
        // Toggle button visibility
        saveButton.setVisible(edit);
        cancelButton.setVisible(edit);
        editButton.setVisible(!edit);
        deleteButton.setVisible(!edit && !isNewMode);
        backButton.setVisible(!edit && !isNewMode);
    }
    
    private void save() {
        if (binder.validate().isOk()) {
            MerchantDTO merchant = binder.getBean();
            
            try {
                if (isNewMode) {
                    merchantService.save(merchant);
                    Notification.show("Merchant created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Navigate to list after successful creation (per design rule)
                    navigateToList();
                } else {
                    merchantService.update(merchant);
                    Notification.show("Merchant updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Refresh to view mode
                    setEditMode(false);
                }
            } catch (Exception e) {
                log.error("Error saving merchant", e);
                Notification.show("Error saving merchant: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void cancel() {
        if (isNewMode) {
            navigateToList();
        } else {
            // Reload the current entity to discard changes
            if (currentMerchant != null && currentMerchant.getId() != null) {
                MerchantDTO merchant = merchantService.findOneWithAccessControl(currentMerchant.getId(), loggedInUser).orElse(null);
                if (merchant != null) {
                    binder.setBean(merchant);
                    this.currentMerchant = merchant;
                }
            }
            setEditMode(false);
        }
    }
    
    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Merchant");
        dialog.setText("Are you sure you want to delete this merchant? This will fail if the merchant has any clients or transactions.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteMerchant());
        
        dialog.open();
    }
    
    private void deleteMerchant() {
        if (currentMerchant != null && currentMerchant.getId() != null) {
            try {
                merchantService.delete(currentMerchant.getId());
                Notification.show("Merchant deleted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigateToList();
            } catch (Exception e) {
                log.error("Error deleting merchant", e);
                // Check if it's a constraint violation
                String errorMsg = e.getMessage();
                if (errorMsg != null && (errorMsg.contains("constraint") || errorMsg.contains("foreign key"))) {
                    Notification.show("Cannot delete merchant: This merchant has associated clients or transactions. Please remove them first.", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else {
                    Notification.show("Error deleting merchant: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        }
    }
    
    protected void navigateToList() {
        getUI().ifPresent(ui -> ui.navigate(MerchantListView.class));
    }
}

