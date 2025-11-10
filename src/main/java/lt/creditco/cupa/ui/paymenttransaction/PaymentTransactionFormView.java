package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;

import java.util.Optional;

/**
 * Simplified form view for PaymentTransaction - can be enhanced later.
 */
@Route(value = "payment-transactions/edit", layout = MainLayout.class)
@PageTitle("Edit Payment Transaction | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN })
public class PaymentTransactionFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final PaymentTransactionService paymentTransactionService;
    private final Binder<PaymentTransactionDTO> binder = new Binder<>(PaymentTransactionDTO.class);
    private final TextField orderIdField = new TextField("Order ID");
    
    public PaymentTransactionFormView(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
        
        setSizeFull();
        setPadding(true);
        
        add(new H2("Payment Transaction (Simplified Form)"));
        
        FormLayout formLayout = new FormLayout();
        orderIdField.setReadOnly(true);
        formLayout.add(orderIdField);
        add(formLayout);
        
        Button backButton = new Button("Back", e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionListView.class)));
        add(new HorizontalLayout(backButton));
        
        binder.bindInstanceFields(this);
    }
    
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.equals("new")) {
            Optional<PaymentTransactionDTO> txOpt = paymentTransactionService.findOne(parameter);
            txOpt.ifPresent(binder::setBean);
        }
    }
}

