package lt.creditco.cupa.ui.merchant;

import org.springframework.context.annotation.Scope;

import com.bpmid.vapp.base.ui.MainLayout;
import lt.creditco.cupa.security.AuthoritiesConstants;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;

@Route(value = "merchants/new", layout = MainLayout.class)
@PageTitle("New Merchant | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })

public class MerchantNewRouteHandler extends MerchantDetailView {

    public MerchantNewRouteHandler(MerchantService merchantService, CupaUserService cupaUserService) {
        super(merchantService, cupaUserService);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String merchantId) {
        super.setParameter(event, null);
    }

}
