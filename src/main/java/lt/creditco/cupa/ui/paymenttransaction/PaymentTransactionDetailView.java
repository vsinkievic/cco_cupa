package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.config.Constants;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaApiBusinessLogicService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.ui.util.JsonDisplayComponent;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.springframework.context.annotation.Scope;

/**
 * Vaadin view for viewing Payment Transaction details (read-only).
 * No edit mode - transactions are managed by the payment gateway.
 */
@Route(value = "payment-transactions", layout = MainLayout.class)
@PageTitle("Payment Transaction Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class PaymentTransactionDetailView extends VerticalLayout implements HasUrlParameter<String> {

    protected final PaymentTransactionService paymentTransactionService;
    protected final MerchantService merchantService;
    protected final CupaApiBusinessLogicService businessLogicService;
    protected final CupaUserService cupaUserService;
    protected final CupaUser loggedInUser;
    protected final Binder<PaymentTransactionDTO> binder = new Binder<>(PaymentTransactionDTO.class);
    
    // Section 1: Transaction Details
    private final TextField orderIdField = new TextField("Order ID");
    private final TextField amountCurrencyField = new TextField("Amount");
    private final TextField requestTimestampField = new TextField("Request Timestamp");
    private final TextField balanceField = new TextField("Balance");
    private final TextField paymentBrandField = new TextField("Payment Brand");
    private final TextField statusField = new TextField("Status");
    private final TextField paymentFlowField = new TextField("Payment Flow");
    private final TextArea statusDescriptionField = new TextArea("Status Description");
    
    // Section 2: Client & Merchant
    private final TextField merchantClientIdField = new TextField("Merchant Client ID");
    private final TextField clientNameField = new TextField("Client Name");
    private final TextField merchantIdField = new TextField("Merchant ID");
    private final TextField merchantNameField = new TextField("Merchant Name");
    
    // Section 3: Additional Information
    private final TextField replyUrlField = new TextField("Reply URL");
    private final TextField backofficeUrlField = new TextField("Backoffice URL");
    private final TextField signatureField = new TextField("Signature");
    private final TextField signatureVersionField = new TextField("Signature Version");
    private final TextField gatewayTransactionIdField = new TextField("Gateway Transaction ID");
    private final TextField callbackTimestampField = new TextField("Callback Timestamp");
    
    // Section 4: Audit Trail (JSON Components) (3-25 rows for payment transactions)
    private final JsonDisplayComponent requestDataComponent = new JsonDisplayComponent().setRowRange(3, 25);
    private final JsonDisplayComponent initialResponseDataComponent = new JsonDisplayComponent().setRowRange(3, 25);
    private final JsonDisplayComponent callbackDataComponent = new JsonDisplayComponent().setRowRange(3, 25);
    private final JsonDisplayComponent echoDataComponent = new JsonDisplayComponent().setRowRange(3, 25);
    private final JsonDisplayComponent lastQueryDataComponent = new JsonDisplayComponent().setRowRange(3, 25);
    
    // Section 5: System Audit
    private final TextField createdByField = new TextField("Created By");
    private final TextField createdDateField = new TextField("Created Date");
    private final TextField lastModifiedByField = new TextField("Last Modified By");
    private final TextField lastModifiedDateField = new TextField("Last Modified Date");
    
    // Buttons
    private final Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
    private final Button queryGatewayButton = new Button("Query Gateway", VaadinIcon.SEARCH.create());
    private final Button backButton = new Button("Back to List");
    
    private final H2 titleLabel = new H2();
    private final HorizontalLayout headerLayout = new HorizontalLayout();
    
    protected PaymentTransactionDTO currentTransaction;
    
    // Auto-refresh for PENDING status
    private Registration autoRefreshRegistration; // Stores the poll listener registration
    private final int AUTO_REFRESH_INTERVAL_MS = 30000; // 30 seconds
    
    public PaymentTransactionDetailView(
            PaymentTransactionService paymentTransactionService,
            MerchantService merchantService,
            CupaApiBusinessLogicService businessLogicService,
            CupaUserService cupaUserService) {
        this.paymentTransactionService = paymentTransactionService;
        this.merchantService = merchantService;
        this.businessLogicService = businessLogicService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(createHeader());
        add(createTransactionOverviewSection());
        add(createClientMerchantSection());
        add(createAdditionalInformationSection());
        add(createAuditTrailSection());
        add(createSystemAuditSection());
        
        configureFields();
        configureBinder();
        configureButtons();
        setReadOnlyMode();
    }
    
    private HorizontalLayout createHeader() {
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        queryGatewayButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        headerLayout.add(titleLabel, refreshButton, queryGatewayButton, backButton);
        headerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerLayout.setWidthFull();
        headerLayout.expand(titleLabel);
        headerLayout.setSpacing(true);
        
        return headerLayout;
    }
    
    private VerticalLayout createTransactionOverviewSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        
        H3 title = new H3("Transaction Details");
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(orderIdField, amountCurrencyField);
        formLayout.add(requestTimestampField, balanceField);
        formLayout.add(paymentBrandField, statusField);
        formLayout.add(paymentFlowField, statusDescriptionField);
        
        section.add(title, formLayout);
        return section;
    }
    
    private VerticalLayout createClientMerchantSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        
        H3 title = new H3("Client & Merchant");
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(merchantClientIdField, clientNameField);
        formLayout.add(merchantIdField, merchantNameField);
        
        section.add(title, formLayout);
        return section;
    }
    
    private VerticalLayout createAdditionalInformationSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        
        H3 title = new H3("Additional Information");
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(replyUrlField, signatureField);
        formLayout.add(backofficeUrlField, signatureVersionField);
        formLayout.add(gatewayTransactionIdField, callbackTimestampField);
        
        section.add(title, formLayout);
        return section;
    }
    
    private VerticalLayout createAuditTrailSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(false);
        
        H3 title = new H3("Audit Trail");
        
        H3 requestTitle = new H3("Request Data");
        requestTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        H3 responseTitle = new H3("Initial Response Data");
        responseTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        H3 callbackTitle = new H3("Callback Data");
        callbackTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        H3 echoTitle = new H3("Echo");
        echoTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        H3 queryTitle = new H3("Last Query Data");
        queryTitle.getStyle().set("font-size", "var(--lumo-font-size-m)");
        
        section.add(title);
        section.add(requestTitle, requestDataComponent);
        section.add(responseTitle, initialResponseDataComponent);
        section.add(callbackTitle, callbackDataComponent);
        section.add(echoTitle, echoDataComponent);
        section.add(queryTitle, lastQueryDataComponent);
        
        // Hide JSON fields for non-admin/creditco users
        if (!isAdminOrCreditco()) {
            section.setVisible(false);
        }
        
        return section;
    }
    
    private VerticalLayout createSystemAuditSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        
        H3 title = new H3("System Audit");
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(createdByField, createdDateField);
        formLayout.add(lastModifiedByField, lastModifiedDateField);
        
        section.add(title, formLayout);
        return section;
    }
    
    private void configureFields() {
        // Configure all fields as full width
        // Section 1: Transaction Details
        orderIdField.setWidthFull();
        amountCurrencyField.setWidthFull();
        requestTimestampField.setWidthFull();
        balanceField.setWidthFull();
        paymentBrandField.setWidthFull();
        statusField.setWidthFull();
        paymentFlowField.setWidthFull();
        statusDescriptionField.setWidthFull();
        statusDescriptionField.setHeight("60px");
        
        // Section 2: Client & Merchant
        merchantClientIdField.setWidthFull();
        clientNameField.setWidthFull();
        merchantIdField.setWidthFull();
        merchantNameField.setWidthFull();
        
        // Section 3: Additional Information
        replyUrlField.setWidthFull();
        backofficeUrlField.setWidthFull();
        signatureField.setWidthFull();
        signatureVersionField.setWidthFull();
        gatewayTransactionIdField.setWidthFull();
        callbackTimestampField.setWidthFull();
        
        // Section 5: System Audit
        createdByField.setWidthFull();
        createdDateField.setWidthFull();
        lastModifiedByField.setWidthFull();
        lastModifiedDateField.setWidthFull();
    }
    
    private void configureBinder() {
        // Section 1: Transaction Details
        binder.forField(orderIdField).bind(PaymentTransactionDTO::getOrderId, (dto, value) -> {});
        
        // Combined amount and currency field
        binder.forField(amountCurrencyField).bind(
            dto -> {
                if (dto.getAmount() != null && dto.getCurrency() != null) {
                    return dto.getAmount().toString() + " " + dto.getCurrency().toString();
                } else if (dto.getAmount() != null) {
                    return dto.getAmount().toString();
                }
                return "";
            },
            (dto, value) -> {}
        );
        
        binder.forField(requestTimestampField).bind(
            dto -> dto.getRequestTimestamp() != null ? dto.getRequestTimestamp().toString() : "",
            (dto, value) -> {}
        );
        
        // Balance field (currently just shows the balance, merchant currency would need to be added to DTO)
        binder.forField(balanceField).bind(
            dto -> dto.getBalance() != null ? dto.getBalance().toString() : "",
            (dto, value) -> {}
        );
        
        binder.forField(paymentBrandField).bind(
            dto -> dto.getPaymentBrand() != null ? dto.getPaymentBrand().toString() : "",
            (dto, value) -> {}
        );
        
        binder.forField(statusField).bind(
            dto -> dto.getStatus() != null ? dto.getStatus().toString() : "",
            (dto, value) -> {}
        );
        
        binder.forField(paymentFlowField).bind(
            dto -> dto.getPaymentFlow() != null ? dto.getPaymentFlow().toString() : "",
            (dto, value) -> {}
        );
        
        binder.forField(statusDescriptionField).bind(PaymentTransactionDTO::getStatusDescription, (dto, value) -> {});
        
        // Section 2: Client & Merchant
        binder.forField(merchantClientIdField).bind(PaymentTransactionDTO::getMerchantClientId, (dto, value) -> {});
        binder.forField(clientNameField).bind(PaymentTransactionDTO::getClientName, (dto, value) -> {});
        binder.forField(merchantIdField).bind(PaymentTransactionDTO::getMerchantId, (dto, value) -> {});
        binder.forField(merchantNameField).bind(PaymentTransactionDTO::getMerchantName, (dto, value) -> {});
        
        // Section 3: Additional Information
        binder.forField(replyUrlField).bind(PaymentTransactionDTO::getReplyUrl, (dto, value) -> {});
        binder.forField(backofficeUrlField).bind(PaymentTransactionDTO::getBackofficeUrl, (dto, value) -> {});
        binder.forField(signatureField).bind(PaymentTransactionDTO::getSignature, (dto, value) -> {});
        binder.forField(signatureVersionField).bind(PaymentTransactionDTO::getSignatureVersion, (dto, value) -> {});
        binder.forField(gatewayTransactionIdField).bind(PaymentTransactionDTO::getGatewayTransactionId, (dto, value) -> {});
        binder.forField(callbackTimestampField).bind(
            dto -> dto.getCallbackTimestamp() != null ? dto.getCallbackTimestamp().toString() : "",
            (dto, value) -> {}
        );
        
        // Section 5: System Audit
        binder.forField(createdByField).bind(PaymentTransactionDTO::getCreatedBy, (dto, value) -> {});
        binder.forField(createdDateField).bind(
            dto -> dto.getCreatedDate() != null ? dto.getCreatedDate().toString() : "",
            (dto, value) -> {}
        );
        binder.forField(lastModifiedByField).bind(PaymentTransactionDTO::getLastModifiedBy, (dto, value) -> {});
        binder.forField(lastModifiedDateField).bind(
            dto -> dto.getLastModifiedDate() != null ? dto.getLastModifiedDate().toString() : "",
            (dto, value) -> {}
        );
    }
    
    private void configureButtons() {
        refreshButton.addClickListener(e -> refreshTransaction());
        queryGatewayButton.addClickListener(e -> queryGateway());
        backButton.addClickListener(e -> navigateToList());
    }
    
    private void setReadOnlyMode() {
        binder.setReadOnly(true);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String transactionId) {
        if (transactionId == null) {
            Notification.show("Invalid transaction ID", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
            return;
        }
        
        log.debug("Loading payment transaction: {} for user: {}", transactionId, loggedInUser.getLogin());
        loadTransaction(transactionId);
    }
    
    private void loadTransaction(String transactionId) {
        PaymentTransactionDTO transaction = paymentTransactionService
            .findOneWithAccessControl(transactionId, loggedInUser)
            .orElse(null);
        
        if (transaction != null) {
            this.currentTransaction = transaction;
            binder.setBean(transaction);
            updateJsonComponents(transaction);
            updateStatusField(transaction);
            updateHeader();
            updateQueryGatewayButtonState(transaction);
            startAutoRefreshIfPending(transaction);
        } else {
            Notification.show("Transaction not found or access denied", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }
    
    private void updateHeader() {
        if (currentTransaction != null && currentTransaction.getOrderId() != null) {
            titleLabel.setText("Payment Transaction: " + currentTransaction.getOrderId());
        } else {
            titleLabel.setText("Payment Transaction Details");
        }
    }
    
    private void updateJsonComponents(PaymentTransactionDTO transaction) {
        log.debug("Updating JSON components for transaction: {}", transaction.getId());
        
        String requestData = transaction.getRequestData();
        String initialResponseData = transaction.getInitialResponseData();
        String callbackData = transaction.getCallbackData();
        String echo = transaction.getEcho();
        String lastQueryData = transaction.getLastQueryData();
        
        log.debug("Request data type: {}, length: {}", 
            requestData != null ? requestData.getClass().getName() : "null",
            requestData != null ? requestData.length() : 0);
        
        requestDataComponent.setJsonContent(requestData);
        initialResponseDataComponent.setJsonContent(initialResponseData);
        callbackDataComponent.setJsonContent(callbackData);
        echoDataComponent.setJsonContent(echo);
        lastQueryDataComponent.setJsonContent(lastQueryData);
    }
    
    private void updateStatusField(PaymentTransactionDTO transaction) {
        if (transaction.getStatus() == null) {
            return;
        }
        
        statusField.setValue(transaction.getStatus().name());
        
        // Remove all status classes first
        statusField.removeClassName("status-success");
        statusField.removeClassName("status-error");
        statusField.removeClassName("status-pending");
        statusField.removeClassName("status-warning");
        
        // Add appropriate class based on status
        switch (transaction.getStatus()) {
            case SUCCESS:
            case QUERY_SUCCESS:
                statusField.addClassName("status-success");
                break;
            case FAILED:
            case CANCELLED:
                statusField.addClassName("status-error");
                break;
            case PENDING:
            case AWAITING_CALLBACK:
                statusField.addClassName("status-pending");
                break;
            case ABANDONED:
            case REFUNDED:
                statusField.addClassName("status-warning");
                break;
            default:
                break;
        }
    }
    
    private void refreshTransaction() {
        if (currentTransaction == null || currentTransaction.getId() == null) {
            return;
        }
        
        PaymentTransactionDTO updated = paymentTransactionService
            .findOneWithAccessControl(currentTransaction.getId(), loggedInUser)
            .orElse(null);
        
        if (updated != null) {
            this.currentTransaction = updated;
            binder.setBean(updated);
            updateJsonComponents(updated);
            updateStatusField(updated);
            updateHeader();
            updateQueryGatewayButtonState(updated);
            
            Notification.show("Transaction refreshed")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Update auto-refresh based on new status
            startAutoRefreshIfPending(updated);
        } else {
            Notification.show("Failed to refresh transaction")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void queryGateway() {
        if (currentTransaction == null || currentTransaction.getId() == null) {
            return;
        }
        
        try {
            // Get merchant for API key
            MerchantDTO merchant = merchantService
                .findOneWithAccessControl(currentTransaction.getMerchantId(), loggedInUser)
                .orElse(null);
            
            if (merchant == null) {
                Notification.show("Merchant not found")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            String merchantApiKey = getMerchantApiKey(merchant);
            
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
                businessLogicService.extractBusinessContext(wrappedRequest, null, 
                    () -> loggedInUser.getLogin());
            
            // Query gateway
            PaymentTransactionDTO updated = paymentTransactionService
                .queryPaymentFromGateway(currentTransaction.getId(), context);
            
            // Update view
            this.currentTransaction = updated;
            binder.setBean(updated);
            updateJsonComponents(updated);
            updateStatusField(updated);
            updateQueryGatewayButtonState(updated);
            
            Notification.show("Gateway queried successfully")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            // Update auto-refresh based on new status
            startAutoRefreshIfPending(updated);
                
        } catch (Exception e) {
            log.error("Error querying gateway", e);
            Notification.show("Error querying gateway: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
    
    private void startAutoRefreshIfPending(PaymentTransactionDTO transaction) {
        // Cancel existing auto-refresh
        stopAutoRefresh();
        
        if (transaction == null || transaction.getStatus() == null) {
            return;
        }
        
        if (transaction.getStatus() == TransactionStatus.PENDING || 
            transaction.getStatus() == TransactionStatus.AWAITING_CALLBACK) {
            // Use UI.getCurrent() to get the UI even during navigation
            UI ui = UI.getCurrent();
            if (ui != null) {
                log.debug("Starting auto-refresh for PENDING transaction: {}", transaction.getId());
                ui.setPollInterval(AUTO_REFRESH_INTERVAL_MS);
                
                // Add poll listener and store the registration
                autoRefreshRegistration = ui.addPollListener(e -> {
                    log.debug("Auto-refresh poll triggered for transaction: {}", currentTransaction.getId());
                    refreshTransaction();
                    
                    // Stop auto-refresh if status changed from PENDING
                    if (currentTransaction != null && 
                        currentTransaction.getStatus() != TransactionStatus.PENDING &&
                        currentTransaction.getStatus() != TransactionStatus.AWAITING_CALLBACK) {
                        log.debug("Stopping auto-refresh, status is now: {}", currentTransaction.getStatus());
                        stopAutoRefresh();
                    }
                });
            } else {
                log.warn("UI not available for auto-refresh, transaction: {}", transaction.getId());
            }
        }
    }
    
    private void stopAutoRefresh() {
        // Remove the poll listener registration if it exists
        if (autoRefreshRegistration != null) {
            autoRefreshRegistration.remove();
            autoRefreshRegistration = null;
            log.debug("Auto-refresh listener removed");
        }
        
        // Stop polling
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.setPollInterval(-1); // Disable polling
            log.debug("Auto-refresh stopped");
        }
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stopAutoRefresh(); // Clean up when view is detached
    }
    
    protected void navigateToList() {
        getUI().ifPresent(ui -> ui.navigate(PaymentTransactionListView.class));
    }
    
    /**
     * Checks if the current user has ADMIN or CREDITCO role.
     */
    private boolean isAdminOrCreditco() {
        return loggedInUser.getAuthorities().stream()
            .map(authority -> authority.getName())
            .anyMatch(authorityName -> 
                AuthoritiesConstants.ADMIN.equals(authorityName) || 
                AuthoritiesConstants.CREDITCO.equals(authorityName)
            );
    }
    
    /**
     * Updates the Query Gateway button enabled state based on user role and transaction status.
     * - ADMIN/CREDITCO: always enabled
     * - Others: only enabled for PENDING or AWAITING_CALLBACK status
     */
    private void updateQueryGatewayButtonState(PaymentTransactionDTO transaction) {
        if (isAdminOrCreditco()) {
            // Admin and CreditCo can always query
            queryGatewayButton.setEnabled(true);
        } else {
            // Others can only query PENDING or AWAITING_CALLBACK transactions
            boolean isPending = transaction != null && 
                (transaction.getStatus() == TransactionStatus.PENDING || 
                 transaction.getStatus() == TransactionStatus.AWAITING_CALLBACK);
            queryGatewayButton.setEnabled(isPending);
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
            return "/vaadin/payment-transactions/view";
        }
        
        @Override
        public String getMethod() {
            return "GET";
        }
        
        @Override
        public String getRemoteAddr() {
            return "127.0.0.1";
        }
    }
}
