package lt.creditco.cupa.ui.merchant;

import com.bpmid.vapp.base.ui.FormMode;
import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
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
import lt.creditco.cupa.ui.components.DailyAmountLimitField;

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
    private final BreadcrumbBar breadcrumbBar = new BreadcrumbBar();
    
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
    
    // PROD environment fields (renamed to LIVE)
    private final TextField cupaProdApiKeyField = new TextField("CUPA LIVE API Key");
    private final TextField remoteProdUrlField = new TextField("Remote LIVE URL");
    private final TextField remoteProdMerchantIdField = new TextField("Remote LIVE Merchant ID");
    private final TextField remoteProdMerchantKeyField = new TextField("Remote LIVE Merchant Key");
    private final TextField remoteProdApiKeyField = new TextField("Remote LIVE API Key");
    
    // TEST environment prefixes and limits
    private final TextField testClientIdPrefixField = new TextField("Client ID Prefix");
    private final TextField testOrderIdPrefixField = new TextField("Order ID Prefix");
    private final DailyAmountLimitField testDailyAmountLimitField;
    
    // LIVE environment prefixes and limits
    private final TextField liveClientIdPrefixField = new TextField("Client ID Prefix");
    private final TextField liveOrderIdPrefixField = new TextField("Order ID Prefix");
    private final DailyAmountLimitField liveDailyAmountLimitField;
    
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
    private FormMode currentMode = FormMode.VIEW;
    
    public MerchantDetailView(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Initialize DailyAmountLimitField instances with UserService
        this.testDailyAmountLimitField = new DailyAmountLimitField(cupaUserService, "Daily Transaction Limit");
        this.liveDailyAmountLimitField = new DailyAmountLimitField(cupaUserService, "Daily Transaction Limit");
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(breadcrumbBar, headerLayout);
        add(createBasicInfoSection());
        add(createEnvironmentCredentialsSection());
        
        configureFields();
        configureBinder();
        configureButtons();
        
        updateHeader();
    }

    public void newMerchant() {
        MerchantDTO newMerchant = new MerchantDTO();
        newMerchant.setMode(MerchantMode.TEST);
        newMerchant.setStatus(MerchantStatus.ACTIVE);
        newMerchant.setBalance(java.math.BigDecimal.ZERO);
        newMerchant.setCurrency(Currency.USD);
        this.currentMerchant = newMerchant;
        binder.setBean(newMerchant);
        setFormMode(FormMode.NEW);
    }
    
    private void updateHeader() {
        headerLayout.removeAll();
        
        if (currentMode.isNew()) {
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
        
        // Add TEST limits section
        testSection.add(createLimitsSection("TEST", testClientIdPrefixField, testOrderIdPrefixField, testDailyAmountLimitField));
        
        // Create LIVE environment section
        H3 prodSectionTitle = new H3("LIVE Environment Credentials");
        
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
        
        // Add LIVE limits section
        prodSection.add(createLimitsSection("LIVE", liveClientIdPrefixField, liveOrderIdPrefixField, liveDailyAmountLimitField));
        
        // Create horizontal layout to hold both sections side by side
        HorizontalLayout horizontalLayout = new HorizontalLayout(testSection, prodSection);
        horizontalLayout.setWidthFull();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setPadding(false);
        horizontalLayout.expand(testSection, prodSection);
        
        return horizontalLayout;
    }
    
    private VerticalLayout createLimitsSection(String environmentLabel, TextField clientPrefix, 
                                               TextField orderPrefix, DailyAmountLimitField limitField) {
        H3 sectionTitle = new H3("Limits and Requirements (" + environmentLabel + ")");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        clientPrefix.setHelperText("Max 10 characters. Must be unique across all merchants.");
        orderPrefix.setHelperText("Max 10 characters. Must be unique across all merchants.");
        
        formLayout.add(clientPrefix, orderPrefix);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout, limitField);
        section.setPadding(false);
        section.setSpacing(true);
        // Add top margin for visual separation from fields above (same spacing as between sections)
        section.getStyle().set("margin-top", "var(--lumo-space-l)");
        
        return section;
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
        
        // Configure prefix fields
        testClientIdPrefixField.setWidthFull();
        testClientIdPrefixField.setMaxLength(10);
        testOrderIdPrefixField.setWidthFull();
        testOrderIdPrefixField.setMaxLength(10);
        liveClientIdPrefixField.setWidthFull();
        liveClientIdPrefixField.setMaxLength(10);
        liveOrderIdPrefixField.setWidthFull();
        liveOrderIdPrefixField.setMaxLength(10);
        
        // Configure limit fields
        testDailyAmountLimitField.setWidthFull();
        liveDailyAmountLimitField.setWidthFull();
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
        
        // Prefix fields
        binder.forField(testClientIdPrefixField).bind(MerchantDTO::getTestClientIdPrefix, MerchantDTO::setTestClientIdPrefix);
        binder.forField(testOrderIdPrefixField).bind(MerchantDTO::getTestOrderIdPrefix, MerchantDTO::setTestOrderIdPrefix);
        binder.forField(liveClientIdPrefixField).bind(MerchantDTO::getLiveClientIdPrefix, MerchantDTO::setLiveClientIdPrefix);
        binder.forField(liveOrderIdPrefixField).bind(MerchantDTO::getLiveOrderIdPrefix, MerchantDTO::setLiveOrderIdPrefix);
        
        // Daily amount limit fields
        binder.forField(testDailyAmountLimitField).bind(MerchantDTO::getTestDailyAmountLimit, MerchantDTO::setTestDailyAmountLimit);
        binder.forField(liveDailyAmountLimitField).bind(MerchantDTO::getLiveDailyAmountLimit, MerchantDTO::setLiveDailyAmountLimit);
        
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
    public void setParameter(BeforeEvent event, String merchantId) {
        // Handle "new" mode (when merchantId is null, the /new route was used)
        if (merchantId == null) {
            log.debug("Creating new merchant");
            MerchantDTO newMerchant = new MerchantDTO();
            newMerchant.setMode(MerchantMode.TEST);
            newMerchant.setStatus(MerchantStatus.ACTIVE);
            newMerchant.setBalance(java.math.BigDecimal.ZERO);
            newMerchant.setCurrency(Currency.USD);
            this.currentMerchant = newMerchant;
            binder.setBean(newMerchant);
            setFormMode(FormMode.NEW);
            updateHeader();
            
            // Update breadcrumb for NEW mode
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Merchants", MerchantListView.class)
                    .currentLink("New", MerchantNewRouteHandler.class)
                    .build()
            );
            return;
        }
        
        // Handle existing merchant (view mode)
        log.debug("Loading merchant: {} for user: {}", merchantId, loggedInUser.getLogin());
        MerchantDTO merchant = merchantService.findOneWithAccessControl(merchantId, loggedInUser).orElse(null);
        if (merchant != null) {
            this.currentMerchant = merchant;
            binder.setBean(merchant);
            setFormMode(FormMode.VIEW); // Always start in view mode
            updateHeader();
            
            // Update breadcrumb for existing merchant
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Merchants", MerchantListView.class)
                    .currentLink(merchant.getId(), MerchantDetailView.class, merchantId)
                    .build()
            );
        } else {
            breadcrumbBar.setItems(
                Breadcrumbs.builder()
                    .home()
                    .link("Merchants", MerchantListView.class)
                    .current("Not Found")
                    .build()
            );
            Notification.show("Merchant not found or access denied", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }
    
    protected void setFormMode(FormMode mode) {
        this.currentMode = mode;
        binder.setReadOnly(!mode.isEditable());
        
        // ID field: editable only in new mode, readonly in edit/view modes
        idField.setReadOnly(!mode.isNew());
        
        // Balance field: always readonly (system managed)
        balanceField.setReadOnly(true);
        
        // Currency field: editable in both create and edit modes
        currencyField.setReadOnly(mode.isView());
        
        // Show/hide generate buttons
        generateTestKeyButton.setVisible(mode.isEditable());
        generateProdKeyButton.setVisible(mode.isEditable());
        
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
            MerchantDTO merchant = binder.getBean();
            
            try {
                if (currentMode.isNew()) {
                    merchantService.save(merchant);
                    Notification.show("Merchant created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Navigate to list after successful creation (per design rule)
                    navigateToList();
                } else {
                    this.currentMerchant = merchantService.save(merchant);
                    binder.setBean(this.currentMerchant);
                    Notification.show("Merchant updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Refresh to view mode
                    setFormMode(FormMode.VIEW);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Validation error saving merchant: {}", e.getMessage());
                Notification.show("Validation error: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        if (currentMode.isNew()) {
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
            setFormMode(FormMode.VIEW);
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

