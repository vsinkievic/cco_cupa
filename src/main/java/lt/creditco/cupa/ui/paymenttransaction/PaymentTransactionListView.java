package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.springframework.data.domain.PageRequest;

/**
 * Vaadin view for listing Payment Transactions.
 */
@Route(value = "payment-transactions", layout = MainLayout.class)
@PageTitle("Payment Transactions | CUPA")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class PaymentTransactionListView extends VerticalLayout {

    private final PaymentTransactionService paymentTransactionService;
    private final Grid<PaymentTransactionDTO> grid = new Grid<>(PaymentTransactionDTO.class, false);
    
    public PaymentTransactionListView(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
        
        setSizeFull();
        setPadding(true);
        
        add(createToolbar(), createGrid());
        refreshGrid();
    }
    
    private HorizontalLayout createToolbar() {
        Button createButton = new Button("New Transaction", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionFormView.class, "new")));
        
        HorizontalLayout toolbar = new HorizontalLayout(createButton);
        toolbar.setWidthFull();
        return toolbar;
    }
    
    private Grid<PaymentTransactionDTO> createGrid() {
        grid.addColumn(PaymentTransactionDTO::getOrderId).setHeader("Order ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getPaymentBrand).setHeader("Payment Brand").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getAmount).setHeader("Amount").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getCurrency).setHeader("Currency").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getRequestTimestamp).setHeader("Timestamp").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true);
        
        grid.addComponentColumn(tx -> {
            Button viewButton = new Button("View", VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            viewButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionDetailView.class, tx.getId())));
            return viewButton;
        }).setHeader("Actions").setAutoWidth(true);
        
        grid.setSizeFull();
        return grid;
    }
    
    private void refreshGrid() {
        CallbackDataProvider<PaymentTransactionDTO, Void> dataProvider = DataProvider.fromCallbacks(
            query -> {
                var pageable = PageRequest.of(query.getPage(), query.getPageSize());
                return paymentTransactionService.findAll(pageable).stream();
            },
            query -> (int) paymentTransactionService.count()
        );
        grid.setDataProvider(dataProvider);
    }
}

