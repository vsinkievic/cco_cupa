package lt.creditco.cupa.ui;

import com.bpmid.vapp.base.ui.MenuProvider;
import com.bpmid.vapp.domain.Authority;
import com.bpmid.vapp.domain.User;
import com.bpmid.vapp.security.AuthoritiesConstants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouterLink;
import lt.creditco.cupa.ui.audit.AuditLogListView;
import lt.creditco.cupa.ui.client.ClientListView;
import lt.creditco.cupa.ui.clientcard.ClientCardListView;
import lt.creditco.cupa.ui.merchant.MerchantListView;
import lt.creditco.cupa.ui.paymenttransaction.PaymentTransactionListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu provider for CUPA application entities.
 * Provides application-specific menu items for authenticated users.
 */
@org.springframework.stereotype.Component
public class CupaMenuProvider implements MenuProvider {
    
    @Override
    public String getCode() {
        return "cupa-entities";
    }
    
    @Override
    public int getOrder() {
        return 100; // After vapp-default (0) but before vapp-admin (200)
    }
    
    @Override
    public List<Component> getMenuItems(User user) {
        List<Component> items = new ArrayList<>();
        
        // Payment Transactions - available to all authenticated users
        items.add(new RouterLink("Payment Transactions", PaymentTransactionListView.class));
        
        // Merchants - only for admins
        if (isAdmin(user)) {
            items.add(new RouterLink("Merchants", MerchantListView.class));
        }
        
        // Clients - available to all authenticated users
        items.add(new RouterLink("Clients", ClientListView.class));
        
        // Client Cards - available to all authenticated users
        items.add(new RouterLink("Client Cards", ClientCardListView.class));
        
        // Audit Logs - only for admins
        if (isAdmin(user)) {
            items.add(new RouterLink("Audit Logs", AuditLogListView.class));
        }
        
        return items;
    }
    
    /**
     * Checks if the user has admin role.
     */
    private boolean isAdmin(User user) {
        return user != null && user.hasAuthority(AuthoritiesConstants.ADMIN);
    }
}
