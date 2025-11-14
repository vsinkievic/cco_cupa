package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.ui.util.JsonDisplayComponent;

import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;

/**
 * Vaadin view for viewing Payment Transaction details.
 */
@Route(value = "payment-transactions/view", layout = MainLayout.class)
@PageTitle("Payment Transaction Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class PaymentTransactionDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final PaymentTransactionService paymentTransactionService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;

    private PaymentTransactionDTO transaction;
    private final VerticalLayout contentLayout = new VerticalLayout();
    
    public PaymentTransactionDetailView(PaymentTransactionService paymentTransactionService, CupaUserService cupaUserService) {
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        this.paymentTransactionService = paymentTransactionService;
        setSizeFull();
        setPadding(true);
        add(contentLayout);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String transactionId) {
        log.debug("Loading payment transaction: {} for user: {}", transactionId, loggedInUser.getLogin());
        PaymentTransactionDTO transaction = paymentTransactionService.findOneWithAccessControl(transactionId, loggedInUser).orElse(null);
        if (transaction != null) {
            this.transaction = transaction;
            buildContent();
        } else {
            Notification.show("Transaction not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(PaymentTransactionListView.class));
        }
    }
    
    private void buildContent() {
        contentLayout.removeAll();
        
        H2 title = new H2("Payment Transaction: " + transaction.getOrderId());
        Button backButton = new Button("Back", e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionListView.class)));
        HorizontalLayout header = new HorizontalLayout(title, backButton);
        header.setWidthFull();
        header.expand(title);
        
        Div details = new Div();
        details.add(createField("Transaction ID", transaction.getId()));
        details.add(createField("Order ID", transaction.getOrderId()));
        details.add(createField("Status", transaction.getStatus() != null ? transaction.getStatus().toString() : ""));
        details.add(createField("Amount", transaction.getAmount() != null ? transaction.getAmount().toString() : ""));
        details.add(createField("Currency", transaction.getCurrency() != null ? transaction.getCurrency().toString() : ""));
        details.add(createField("Payment Brand", transaction.getPaymentBrand() != null ? transaction.getPaymentBrand().toString() : ""));
        
        H3 requestDataTitle = new H3("Request Data");
        JsonDisplayComponent requestJson = new JsonDisplayComponent();
        requestJson.setJsonContent(transaction.getRequestData());
        
        H3 responseDataTitle = new H3("Response Data");
        JsonDisplayComponent responseJson = new JsonDisplayComponent();
        responseJson.setJsonContent(transaction.getInitialResponseData());
        
        contentLayout.add(header, details, requestDataTitle, requestJson, responseDataTitle, responseJson);
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

