package lt.creditco.cupa.ui.merchant;

import org.springframework.context.annotation.Scope;

import com.bpmid.vapp.base.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.MerchantDTO;

/**
 * Vaadin view for viewing Merchant details.
 */
@Route(value = "merchants/view", layout = MainLayout.class)
@PageTitle("Merchant Details | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO })
@Slf4j
public class MerchantDetailView extends VerticalLayout implements HasUrlParameter<String> {

    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    
    private MerchantDTO merchant;
    
    private final VerticalLayout contentLayout = new VerticalLayout();
    
    public MerchantDetailView(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(contentLayout);
    }
    
    @Override
    public void setParameter(BeforeEvent event, String merchantId) {
        log.debug("Loading merchant: {} for user: {}", merchantId, loggedInUser.getLogin());
        MerchantDTO merchant = merchantService.findOneWithAccessControl(merchantId, loggedInUser).orElse(null);
        if (merchant != null) {
            this.merchant = merchant;
            buildContent();
        } else {
            Notification.show("Merchant not found", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            getUI().ifPresent(ui -> ui.navigate(MerchantListView.class));
        }
    }
    
    private void buildContent() {
        contentLayout.removeAll();
        
        H2 title = new H2("Merchant: " + merchant.getName());
        
        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(MerchantFormView.class, merchant.getId()))
        );
        
        Button backButton = new Button("Back to List");
        backButton.addClickListener(e -> 
            getUI().ifPresent(ui -> ui.navigate(MerchantListView.class))
        );
        
        HorizontalLayout header = new HorizontalLayout(title, editButton, backButton);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.setWidthFull();
        header.expand(title);
        
        contentLayout.add(header);
        contentLayout.add(createBasicInfoSection());
        contentLayout.add(createTestEnvironmentSection());
        contentLayout.add(createProdEnvironmentSection());
    }
    
    private VerticalLayout createBasicInfoSection() {
        H3 sectionTitle = new H3("Basic Information");
        
        Div content = new Div();
        content.add(createField("Merchant ID", merchant.getId()));
        content.add(createField("Name", merchant.getName()));
        content.add(createField("Mode", merchant.getMode() != null ? merchant.getMode().toString() : ""));
        content.add(createField("Status", merchant.getStatus() != null ? merchant.getStatus().toString() : ""));
        content.add(createField("Balance", merchant.getBalance() != null ? merchant.getBalance().toString() : ""));
        content.add(createField("Currency", merchant.getCurrency() != null ? merchant.getCurrency().toString() : ""));
        
        VerticalLayout section = new VerticalLayout(sectionTitle, content);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createTestEnvironmentSection() {
        H3 sectionTitle = new H3("TEST Environment Credentials");
        
        Div content = new Div();
        content.add(createField("CUPA Test API Key", merchant.getCupaTestApiKey()));
        content.add(createField("Remote Test URL", merchant.getRemoteTestUrl()));
        content.add(createField("Remote Test Merchant ID", merchant.getRemoteTestMerchantId()));
        content.add(createField("Remote Test Merchant Key", maskSensitiveData(merchant.getRemoteTestMerchantKey())));
        content.add(createField("Remote Test API Key", maskSensitiveData(merchant.getRemoteTestApiKey())));
        
        VerticalLayout section = new VerticalLayout(sectionTitle, content);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
    }
    
    private VerticalLayout createProdEnvironmentSection() {
        H3 sectionTitle = new H3("PRODUCTION Environment Credentials");
        
        Div content = new Div();
        content.add(createField("CUPA Production API Key", merchant.getCupaProdApiKey()));
        content.add(createField("Remote Production URL", merchant.getRemoteProdUrl()));
        content.add(createField("Remote Production Merchant ID", merchant.getRemoteProdMerchantId()));
        content.add(createField("Remote Production Merchant Key", maskSensitiveData(merchant.getRemoteProdMerchantKey())));
        content.add(createField("Remote Production API Key", maskSensitiveData(merchant.getRemoteProdApiKey())));
        
        VerticalLayout section = new VerticalLayout(sectionTitle, content);
        section.setPadding(false);
        section.setSpacing(false);
        
        return section;
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
    
    private String maskSensitiveData(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}

