package lt.creditco.cupa.ui.merchant;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Scope;

/**
 * Vaadin view for creating/editing a Merchant.
 */
@Route(value = "merchants/edit", layout = MainLayout.class)
@PageTitle("Edit Merchant | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
@Slf4j
public class MerchantFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Binder<MerchantDTO> binder = new BeanValidationBinder<>(MerchantDTO.class);
    
    private String merchantId;
    
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
    
    public MerchantFormView(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Merchant Details");
        add(title);
        
        add(createBasicInfoSection());
        add(createTestEnvironmentSection());
        add(createProdEnvironmentSection());
        add(createButtonBar());
        
        configureFields();
        configureBinder();
    }
    
    private VerticalLayout createBasicInfoSection() {
        H3 sectionTitle = new H3("Basic Information");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        idField.setReadOnly(true);
        idField.setHelperText("Auto-generated on creation");
        
        nameField.setRequiredIndicatorVisible(true);
        modeField.setRequiredIndicatorVisible(true);
        statusField.setRequiredIndicatorVisible(true);
        
        formLayout.add(idField, nameField, modeField, statusField, balanceField, currencyField);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createTestEnvironmentSection() {
        H3 sectionTitle = new H3("TEST Environment Credentials");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        Button generateTestKey = new Button("Generate", VaadinIcon.REFRESH.create());
        generateTestKey.addClickListener(e -> {
            cupaTestApiKeyField.setValue(UUID.randomUUID().toString());
            Notification.show("New API key generated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        cupaTestApiKeyField.setSuffixComponent(generateTestKey);
        
        formLayout.add(cupaTestApiKeyField, 2);
        formLayout.add(remoteTestUrlField, 2);
        formLayout.add(remoteTestMerchantIdField, remoteTestMerchantKeyField);
        formLayout.add(remoteTestApiKeyField, 2);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createProdEnvironmentSection() {
        H3 sectionTitle = new H3("PRODUCTION Environment Credentials");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        Button generateProdKey = new Button("Generate", VaadinIcon.REFRESH.create());
        generateProdKey.addClickListener(e -> {
            cupaProdApiKeyField.setValue(UUID.randomUUID().toString());
            Notification.show("New API key generated").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        cupaProdApiKeyField.setSuffixComponent(generateProdKey);
        
        formLayout.add(cupaProdApiKeyField, 2);
        formLayout.add(remoteProdUrlField, 2);
        formLayout.add(remoteProdMerchantIdField, remoteProdMerchantKeyField);
        formLayout.add(remoteProdApiKeyField, 2);
        
        VerticalLayout section = new VerticalLayout(sectionTitle, formLayout);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
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
        binder.bindInstanceFields(this);
        binder.addStatusChangeListener(e -> {
            // Enable/disable save button based on validation
        });
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.equals("new")) {
            merchantId = parameter;
            loadMerchant(merchantId);
        } else {
            // New merchant
            MerchantDTO newMerchant = new MerchantDTO();
            newMerchant.setMode(MerchantMode.TEST);
            newMerchant.setStatus(MerchantStatus.ACTIVE);
            binder.setBean(newMerchant);
        }
    }
    
    private void loadMerchant(String id) {
        log.debug("Loading merchant: {} for user: {}", id, loggedInUser.getLogin());
        MerchantDTO merchant = merchantService.findOneWithAccessControl(id, loggedInUser).orElse(null);
        if (merchant != null) {
            binder.setBean(merchant);
        } else {
            Notification.show("Merchant not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            cancel();
        }
    }
    
    private void save() {
        if (binder.validate().isOk()) {
            MerchantDTO merchant = binder.getBean();
            
            try {
                if (merchant.getId() == null || merchant.getId().isEmpty()) {
                    merchant.setId(UUID.randomUUID().toString());
                    merchantService.save(merchant);
                    Notification.show("Merchant created successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    merchantService.update(merchant);
                    Notification.show("Merchant updated successfully")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                
                getUI().ifPresent(ui -> ui.navigate(MerchantListView.class));
            } catch (Exception e) {
                Notification.show("Error saving merchant: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void cancel() {
        getUI().ifPresent(ui -> ui.navigate(MerchantListView.class));
    }
}

