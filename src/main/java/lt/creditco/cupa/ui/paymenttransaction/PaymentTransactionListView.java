package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * Vaadin view for listing Payment Transactions.
 */
@Route(value = "payment-transactions", layout = MainLayout.class)
@PageTitle("Payment Transactions | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class PaymentTransactionListView extends VerticalLayout {

    private final PaymentTransactionService paymentTransactionService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    private final Grid<PaymentTransactionDTO> grid = new Grid<>(PaymentTransactionDTO.class, false);
    private Grid.Column<PaymentTransactionDTO> timestampColumn;
    
    public PaymentTransactionListView(PaymentTransactionService paymentTransactionService, CupaUserService cupaUserService) {
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        this.paymentTransactionService = paymentTransactionService;
        
        setSizeFull();
        setPadding(true);
        
        add(createToolbar(), createGrid());
        refreshGrid();
    }
    
    private HorizontalLayout createToolbar() {
        Button createButton = new Button("New Transaction", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionCreateView.class)));
        
        HorizontalLayout toolbar = new HorizontalLayout(createButton);
        toolbar.setWidthFull();
        return toolbar;
    }
    
    private Grid<PaymentTransactionDTO> createGrid() {
        grid.addColumn(PaymentTransactionDTO::getOrderId).setHeader("Order ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getPaymentBrand).setHeader("Payment Brand").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getAmount).setHeader("Amount").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getCurrency).setHeader("Currency").setSortable(true).setAutoWidth(true);
        timestampColumn = grid.addColumn(PaymentTransactionDTO::getRequestTimestamp).setHeader("Timestamp").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true);
        
        grid.addComponentColumn(tx -> {
            RouterLink viewLink = new RouterLink("", PaymentTransactionDetailView.class, tx.getId());
            viewLink.add(VaadinIcon.EYE.create());
            viewLink.getElement().setAttribute("title", "View Transaction");
            return viewLink;
        }).setHeader(" ").setWidth("70px").setFlexGrow(0);
        
        // Add double-click navigation
        grid.addItemDoubleClickListener(event -> 
            getUI().ifPresent(ui -> ui.navigate(PaymentTransactionDetailView.class, event.getItem().getId()))
        );
        
        grid.setSizeFull();
        return grid;
    }
    
    private void refreshGrid() {
        // Load all payment transactions (with pagination to limit initial load)
        var pageable = PageRequest.of(0, 1000);
        List<PaymentTransactionDTO> allTransactions = paymentTransactionService.findAllWithAccessControl(pageable, loggedInUser).getContent();
        
        ListDataProvider<PaymentTransactionDTO> dataProvider = new ListDataProvider<>(allTransactions);
        grid.setDataProvider(dataProvider);
        
        // Set default sort order: newest records first (by request timestamp descending)
        grid.sort(java.util.Collections.singletonList(
            new GridSortOrder<>(timestampColumn, SortDirection.DESCENDING)
        ));
    }
}

