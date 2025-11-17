package lt.creditco.cupa.ui.clientcard;

import org.springframework.context.annotation.Scope;

import com.bpmid.vapp.base.ui.MainLayout;
import lt.creditco.cupa.security.AuthoritiesConstants;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.service.ClientCardService;
import lt.creditco.cupa.service.ClientService;
import lt.creditco.cupa.service.CupaUserService;

@Route(value = "client-cards/new", layout = MainLayout.class)
@PageTitle("New Client Card | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
public class ClientCardNewRouteHandler extends ClientCardDetailView {

    public ClientCardNewRouteHandler(ClientCardService clientCardService, ClientService clientService, CupaUserService cupaUserService) {
        super(clientCardService, clientService, cupaUserService);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String cardId) {
        super.setParameter(event, null);
    }

}

