package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
import com.bpmid.vapp.web.rest.errors.BadRequestAlertException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.ClientDTO;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Create view for PaymentTransaction - separate from detail view due to complexity.
 * This is for creating NEW transactions only.
 */
@Route(value = "payment-transactions/new", layout = MainLayout.class)
@PageTitle("Create Payment Transaction | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class PaymentTransactionCreateView extends VerticalLayout implements BeforeEnterObserver {

    private final PaymentTransactionService paymentTransactionService;
    private final MerchantService merchantService;
    private final ClientService clientService;
    private final CupaApiBusinessLogicService businessLogicService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private final Binder<PaymentTransactionDTO> binder = new BeanValidationBinder<>(PaymentTransactionDTO.class);
    
    // For cloning functionality
    private PaymentTransactionDTO sourceTransaction;
    
    // Form fields - in order according to plan
    private final ComboBox<MerchantDTO> merchantField = new ComboBox<>("Merchant");
    private final TextField orderIdField = new TextField("Order ID");
    private final ComboBox<ClientDTO> clientField = new ComboBox<>("Client");
    private final BigDecimalField amountField = new BigDecimalField("Amount");
    private final ComboBox<Currency> currencyField = new ComboBox<>("Currency");
    private final ComboBox<PaymentBrand> paymentBrandField = new ComboBox<>("Payment Brand");
    private final ComboBox<PaymentFlow> paymentFlowField = new ComboBox<>("Payment Flow");
    private final TextField replyUrlField = new TextField("Reply URL");
    private final TextField echoField = new TextField("Echo");
    
    // Buttons
    private final Button saveButton = new Button("Create Payment");
    private final Button cancelButton = new Button("Cancel");
    
    public PaymentTransactionCreateView(
            PaymentTransactionService paymentTransactionService,
            MerchantService merchantService,
            ClientService clientService,
            CupaApiBusinessLogicService businessLogicService,
            CupaUserService cupaUserService) {
        this.paymentTransactionService = paymentTransactionService;
        this.merchantService = merchantService;
        this.clientService = clientService;
        this.businessLogicService = businessLogicService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        BreadcrumbBar breadcrumbBar = new BreadcrumbBar(
            Breadcrumbs.builder()
                .home()
                .link("Payment Transactions", PaymentTransactionListView.class)
                .currentLink("New", PaymentTransactionCreateView.class)
                .build()
        );
        
        add(breadcrumbBar, new H2("Create Payment Transaction"));
        add(createFormLayout());
        add(createButtonLayout());
        
        configureFields();
        configureBinder();
        configureButtons();
        loadMerchants();
        initializeDefaults();
        
        // Initialize with empty DTO
        binder.setBean(new PaymentTransactionDTO());
    }
    
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Add fields in order
        formLayout.add(merchantField, 2);
        formLayout.add(orderIdField, clientField);
        formLayout.add(amountField, currencyField);
        formLayout.add(paymentBrandField, paymentFlowField);
        formLayout.add(replyUrlField, 2);
        formLayout.add(echoField, 2);
        
        return formLayout;
    }
    
    private HorizontalLayout createButtonLayout() {
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        return buttonLayout;
    }
    
    private void configureFields() {
        // Merchant field
        merchantField.setWidthFull();
        merchantField.setRequired(true);
        merchantField.setRequiredIndicatorVisible(true);
        merchantField.setItemLabelGenerator(merchant -> merchant.getId() + " | " + merchant.getName());
        
        // Order ID field
        orderIdField.setWidthFull();
        orderIdField.setRequired(true);
        orderIdField.setRequiredIndicatorVisible(true);
        orderIdField.setHelperText("Unique order identifier for this transaction");
        
        // Client field
        clientField.setWidthFull();
        clientField.setRequired(true);
        clientField.setRequiredIndicatorVisible(true);
        clientField.setEnabled(false); // Disabled until merchant selected
        clientField.setHelperText("Type at least 4 characters to search");
        
        // Amount field
        amountField.setWidthFull();
        amountField.setRequired(true);
        amountField.setRequiredIndicatorVisible(true);
        
        // Currency field
        currencyField.setWidthFull();
        currencyField.setRequired(true);
        currencyField.setRequiredIndicatorVisible(true);
        currencyField.setItems(Currency.values());
        
        // Payment Brand field
        paymentBrandField.setWidthFull();
        paymentBrandField.setRequired(true);
        paymentBrandField.setRequiredIndicatorVisible(true);
        paymentBrandField.setItems(PaymentBrand.values());
        
        // Payment Flow field
        paymentFlowField.setWidthFull();
        paymentFlowField.setRequired(true);
        paymentFlowField.setRequiredIndicatorVisible(true);
        paymentFlowField.setItems(PaymentFlow.values());
        
        // Reply URL field
        replyUrlField.setWidthFull();
        replyUrlField.setHelperText("Optional callback URL for payment status updates");
        
        // Echo field
        echoField.setWidthFull();
        echoField.setHelperText("Optional data that will be echoed back in callbacks");
    }
    
    private void configureBinder() {
        // Bind fields with validation
        binder.forField(merchantField)
            .asRequired("Merchant is required")
            .bind(
                dto -> null, // Not bound to DTO, handled separately
                (dto, value) -> {}
            );
        
        binder.forField(orderIdField)
            .asRequired("Order ID is required")
            .withValidator(orderId -> orderId != null && !orderId.trim().isEmpty(), 
                "Order ID cannot be empty")
            .bind(PaymentTransactionDTO::getOrderId, PaymentTransactionDTO::setOrderId);
        
        binder.forField(clientField)
            .asRequired("Client is required")
            .bind(
                dto -> null, // Not directly bound, handled via clientId
                (dto, value) -> {
                    if (value != null) {
                        dto.setClientId(value.getId());
                        dto.setClientName(value.getName());
                        dto.setMerchantClientId(value.getMerchantClientId());
                    }
                }
            );
        
        binder.forField(amountField)
            .asRequired("Amount is required")
            .withValidator(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0,
                "Amount must be greater than 0")
            .bind(PaymentTransactionDTO::getAmount, PaymentTransactionDTO::setAmount);
        
        binder.forField(currencyField)
            .asRequired("Currency is required")
            .bind(PaymentTransactionDTO::getCurrency, PaymentTransactionDTO::setCurrency);
        
        binder.forField(paymentBrandField)
            .asRequired("Payment Brand is required")
            .bind(PaymentTransactionDTO::getPaymentBrand, PaymentTransactionDTO::setPaymentBrand);
        
        binder.forField(paymentFlowField)
            .asRequired("Payment Flow is required")
            .bind(PaymentTransactionDTO::getPaymentFlow, PaymentTransactionDTO::setPaymentFlow);
        
        binder.forField(replyUrlField)
            .withValidator(url -> url == null || url.isEmpty() || url.startsWith("http://") || url.startsWith("https://"),
                "Reply URL must be a valid HTTP/HTTPS URL")
            .bind(PaymentTransactionDTO::getReplyUrl, PaymentTransactionDTO::setReplyUrl);
        
        binder.forField(echoField)
            .bind(PaymentTransactionDTO::getEcho, PaymentTransactionDTO::setEcho);
    }
    
    private void configureButtons() {
        saveButton.addClickListener(e -> createPayment());
        cancelButton.addClickListener(e -> navigateToList());
    }
    
    private void initializeDefaults() {
        // Set default payment flow
        paymentFlowField.setValue(PaymentFlow.ONLINE);
    }
    
    private void loadMerchants() {
        var pageable = PageRequest.of(0, 100);
        List<MerchantDTO> merchants = merchantService.findAllWithAccessControl(pageable, loggedInUser)
            .getContent();
        
        merchantField.setItems(merchants);
        
        // Auto-select if only one merchant
        if (merchants.size() == 1) {
            merchantField.setValue(merchants.get(0));
        }
        // If empty list - just show empty combobox (no error message)
        
        // Add listener to load clients when merchant changes
        merchantField.addValueChangeListener(e -> {
            MerchantDTO selectedMerchant = e.getValue();
            if (selectedMerchant != null) {
                loadClientsForMerchant(selectedMerchant.getId());
                // Set default currency from merchant (only if not cloning)
                if (selectedMerchant.getCurrency() != null && sourceTransaction == null) {
                    currencyField.setValue(selectedMerchant.getCurrency());
                }
            } else {
                clientField.setItems(Collections.emptyList());
                clientField.setEnabled(false);
                clientField.clear();
            }
        });
    }
    
    private void loadClientsForMerchant(String merchantId) {
        if (merchantId == null) {
            clientField.setItems(Collections.emptyList());
            clientField.setEnabled(false);
            return;
        }
        
        MerchantDTO selectedMerchant = merchantField.getValue();
        if (selectedMerchant == null || selectedMerchant.getMode() == null) {
            clientField.setItems(Collections.emptyList());
            clientField.setEnabled(false);
            return;
        }
        
        // Load all clients for merchant (up to 10K) into memory
        var pageable = PageRequest.of(0, 10000);
        List<ClientDTO> clients = clientService.findByMerchantId(merchantId, pageable)
            .getContent()
            .stream()
            .filter(c -> selectedMerchant.getMode().equals(c.getEnvironment()))
            .toList();
        
        // Enhanced label generator - shows ID | Name | Email | Phone
        clientField.setItemLabelGenerator(client -> {
            StringBuilder label = new StringBuilder();
            // Use merchantClientId if available, otherwise use system ID
            label.append(client.getMerchantClientId() != null ? client.getMerchantClientId() : client.getId());
            if (client.getName() != null) {
                label.append(" | ").append(client.getName());
            }
            if (client.getEmailAddress() != null) {
                label.append(" | ").append(client.getEmailAddress());
            }
            if (client.getMobileNumber() != null) {
                label.append(" | ").append(client.getMobileNumber());
            }
            return label.toString();
        });
        
        // Create data provider with client-side filtering
        ListDataProvider<ClientDTO> dataProvider = new ListDataProvider<>(clients);
        
        // Configure filtering - searches across all fields
        clientField.addCustomValueSetListener(e -> {
            // This enables typing to filter
            String filter = e.getDetail();
            if (filter != null && filter.length() >= 4) {
                dataProvider.setFilter(client -> matchesFilter(client, filter));
            } else {
                dataProvider.clearFilters();
            }
        });
        
        clientField.setItems(dataProvider);
        clientField.setEnabled(true);
        
        // If we're cloning, set the client value after clients are loaded
        if (sourceTransaction != null && sourceTransaction.getClientId() != null) {
            String clientIdToSet = sourceTransaction.getClientId();
            clients.stream()
                .filter(c -> clientIdToSet.equals(c.getId()))
                .findFirst()
                .ifPresent(client -> {
                    clientField.setValue(client);
                    log.debug("Client set from cloning: {}", client.getId());
                });
            // Clear source transaction reference after use
            sourceTransaction = null;
        }
    }
    
    private boolean matchesFilter(ClientDTO client, String filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        
        String searchLower = filter.toLowerCase();
        
        // Check merchantClientId
        if (client.getMerchantClientId() != null && 
            client.getMerchantClientId().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        // Check system ID
        if (client.getId() != null && 
            client.getId().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        // Check name
        if (client.getName() != null && 
            client.getName().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        // Check email
        if (client.getEmailAddress() != null && 
            client.getEmailAddress().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        // Check phone
        if (client.getMobileNumber() != null && 
            client.getMobileNumber().toLowerCase().contains(searchLower)) {
            return true;
        }
        
        return false;
    }
    
    private void createPayment() {
        if (!binder.validate().isOk()) {
            Notification.show("Please fix validation errors")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        PaymentTransactionDTO dto = binder.getBean();
        
        // Manually set merchant ID from selected merchant
        MerchantDTO selectedMerchant = merchantField.getValue();
        if (selectedMerchant != null) {
            dto.setMerchantId(selectedMerchant.getId());
            dto.setMerchantName(selectedMerchant.getName());
        }
        
        // Manually set client info from selected client
        ClientDTO selectedClient = clientField.getValue();
        if (selectedClient != null) {
            dto.setClientId(selectedClient.getId());
            dto.setClientName(selectedClient.getName());
            dto.setMerchantClientId(selectedClient.getMerchantClientId());
        }
        
        try {
            // Get merchant API key
            String merchantApiKey = getMerchantApiKey(selectedMerchant);
            
            // Get current Vaadin request and wrap it to inject API key
            VaadinRequest vaadinRequest = VaadinRequest.getCurrent();
            HttpServletRequest baseRequest = null;
            if (vaadinRequest instanceof VaadinServletRequest) {
                baseRequest = ((VaadinServletRequest) vaadinRequest).getHttpServletRequest();
            }
            
            // Create a wrapper request that adds the API key header
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(baseRequest, merchantApiKey);
            
            // Extract business context
            CupaApiContext.CupaApiContextData context = 
                businessLogicService.extractBusinessContext(wrappedRequest, dto, 
                    () -> loggedInUser.getLogin());
            
            // Save transaction (will generate ID, call gateway, etc.)
            PaymentTransactionDTO saved = paymentTransactionService.save(dto, context);
            
            Notification.show("Payment transaction created successfully")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Navigate to DETAIL VIEW (not list)
            getUI().ifPresent(ui -> ui.navigate("payment-transactions/" + saved.getId()));
        } catch (BadRequestAlertException e) {
            log.error("Error creating payment transaction", e.getMessage());
            String errorMessage = e.getBody() == null ? e.getMessage() : e.getBody().getTitle();
            if (errorMessage == null) {
                errorMessage = e.getMessage();
            }
            Notification.show(errorMessage)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Error creating payment transaction", e);
            
            // Check if transaction was saved (has version)
            if (dto.getVersion() != null) {
                // Transaction saved but failed - navigate to view anyway
                Notification.show("Transaction saved but processing failed: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
                getUI().ifPresent(ui -> ui.navigate("payment-transactions/" + dto.getId()));
            } else {
                // Transaction not saved - show error
                Notification.show("Error: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
    
    private String getMerchantApiKey(MerchantDTO merchant) {
        if (merchant == null) {
            return null;
        }
        // Use TEST or PROD key based on merchant mode
        if (merchant.getMode() == MerchantMode.LIVE) {
            return merchant.getCupaProdApiKey();
        } else {
            return merchant.getCupaTestApiKey();
        }
    }
    
    private void navigateToList() {
        getUI().ifPresent(ui -> ui.navigate(PaymentTransactionListView.class));
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        String cloneFromId = queryParameters.getParameters()
            .getOrDefault("cloneFrom", Collections.emptyList())
            .stream()
            .findFirst()
            .orElse(null);
        
        if (cloneFromId != null) {
            loadTransactionForCloning(cloneFromId);
        }
    }
    
    private void loadTransactionForCloning(String transactionId) {
        log.debug("Loading transaction for cloning: {}", transactionId);
        paymentTransactionService
            .findOneWithAccessControl(transactionId, loggedInUser)
            .ifPresent(this::prefillFromTransaction);
    }
    
    private void prefillFromTransaction(PaymentTransactionDTO source) {
        log.debug("Prefilling form from transaction: {}", source.getId());
        
        // Store source transaction for use in merchant change listener
        this.sourceTransaction = source;
        
        // Set merchant first - this will trigger client loading
        if (source.getMerchantId() != null) {
            merchantService.findOneWithAccessControl(source.getMerchantId(), loggedInUser)
                .ifPresent(merchant -> {
                    merchantField.setValue(merchant);
                    // Client will be set after clients are loaded in the merchant change listener
                });
        }
        
        // Set orderId with "_" suffix
        if (source.getOrderId() != null) {
            orderIdField.setValue(source.getOrderId() + "_");
        }
        
        // Set amount and currency
        if (source.getAmount() != null) {
            amountField.setValue(source.getAmount());
        }
        if (source.getCurrency() != null) {
            currencyField.setValue(source.getCurrency());
        }
        
        // Set payment brand and flow
        if (source.getPaymentBrand() != null) {
            paymentBrandField.setValue(source.getPaymentBrand());
        }
        if (source.getPaymentFlow() != null) {
            paymentFlowField.setValue(source.getPaymentFlow());
        }
        
        // Set reply URL and echo
        if (source.getReplyUrl() != null) {
            replyUrlField.setValue(source.getReplyUrl());
        }
        if (source.getEcho() != null) {
            echoField.setValue(source.getEcho());
        }
    }
    
    /**
     * Simple wrapper to inject API key header into the request for business logic extraction.
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final String apiKey;
        
        public HttpServletRequestWrapper(HttpServletRequest request, String apiKey) {
            super(request != null ? request : new EmptyHttpServletRequest());
            this.apiKey = apiKey;
        }
        
        @Override
        public String getHeader(String name) {
            if (Constants.API_KEY_HEADER.equals(name)) {
                return apiKey;
            }
            return super.getHeader(name);
        }
    }
    
    /**
     * Empty HTTP request implementation for cases where Vaadin request is not available.
     */
    private static class EmptyHttpServletRequest extends jakarta.servlet.http.HttpServletRequestWrapper {
        public EmptyHttpServletRequest() {
            super(null);
        }
        
        @Override
        public String getHeader(String name) {
            return null;
        }
        
        @Override
        public String getRequestURI() {
            return "/vaadin/payment-transactions/new";
        }
        
        @Override
        public String getMethod() {
            return "POST";
        }
        
        @Override
        public String getRemoteAddr() {
            return "127.0.0.1";
        }
    }
}
