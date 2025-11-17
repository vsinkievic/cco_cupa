package lt.creditco.cupa.ui.client;

import org.springframework.context.annotation.Scope;

import com.bpmid.vapp.base.ui.MainLayout;
import lt.creditco.cupa.security.AuthoritiesConstants;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;

@Route(value = "clients/new", layout = MainLayout.class)
@PageTitle("New Client | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientNewRouteHandler extends ClientDetailView {

    public ClientNewRouteHandler(ClientService clientService, MerchantService merchantService, CupaUserService cupaUserService) {
        super(clientService, merchantService, cupaUserService);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String clientId) {
        super.setParameter(event, null);
    }

}

