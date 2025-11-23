package lt.creditco.cupa.ui.users;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.bpmid.vapp.base.ui.FormMode;
import com.bpmid.vapp.base.ui.users.UserFormCustomizer;
import com.bpmid.vapp.domain.User;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.dto.MerchantDTO;

/**
 * Customizer for CupaUser that adds merchant selection UI.
 * Implements the UserFormCustomizer pattern to extend vapp-base UserForm
 * with CUPA-specific fields without modifying the base library.
 * 
 * <p>This customizer adds a multi-select combobox for assigning merchants to users.
 * The selection is stored as a comma-separated string in CupaUser.merchantIds.</p>
 */
@Component
public class CupaUserFormCustomizer implements UserFormCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(CupaUserFormCustomizer.class);
    
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    
    // Store the component reference to bind it later
    private MultiSelectComboBox<MerchantDTO> merchantComboBox;
    
    public CupaUserFormCustomizer(MerchantService merchantService, CupaUserService cupaUserService) {
        this.merchantService = merchantService;
        this.cupaUserService = cupaUserService;
    }
    
    @Override
    public boolean supports(User user) {
        return user instanceof CupaUser;
    }
    
    @Override
    public void addCustomFields(FormLayout form, User user) {
        // Create multi-select combobox for merchants
        merchantComboBox = new MultiSelectComboBox<>();
        merchantComboBox.setLabel("Assigned Merchants");
        merchantComboBox.setHelperText("Select merchants this user can access. Admins can access all merchants automatically.");
        merchantComboBox.setPlaceholder("Select merchants...");
        
        // Load available merchants
        List<MerchantDTO> merchants = loadAvailableMerchants();
        merchantComboBox.setItems(merchants);
        merchantComboBox.setItemLabelGenerator(m -> m.getId() + " - " + m.getName());
        
        // Set width
        merchantComboBox.setWidthFull();
        
        // Add to form (will continue after standard fields in the layout)
        form.add(merchantComboBox);
    }
    
    @Override
    public void bindCustomFields(Binder<User> binder, User user) {
        if (!(user instanceof CupaUser)) {
            return;
        }
        
        // Bind the merchantComboBox to CupaUser.merchantIds
        // Use a converter to transform between Set<MerchantDTO> and comma-separated String
        binder.forField(merchantComboBox)
            .withConverter(new MerchantSelectionConverter())
            .bind(
                u -> ((CupaUser)u).getMerchantIds(),
                (u, value) -> ((CupaUser)u).setMerchantIds(value)
            );
    }
    
    @Override
    public boolean validateCustomFields(User user) {
        // Validation is performed in CupaUserService.validateMerchantIds()
        // which throws InvalidMerchantIdsException if merchant IDs are invalid
        return true;
    }
    
    @Override
    public void setFormMode(FormMode mode) {
        if (merchantComboBox != null) {
            // Make field read-only in VIEW mode, editable in NEW and EDIT modes
            boolean isReadOnly = (mode == FormMode.VIEW);
            merchantComboBox.setReadOnly(isReadOnly);
        }
    }
    
    @Override
    public User createNewUser() {
        return new CupaUser();
    }
    
    /**
     * Load available merchants based on the current user's access rights.
     * Admin users can see all merchants, regular users see only their assigned merchants.
     */
    private List<MerchantDTO> loadAvailableMerchants() {
        try {
            CupaUser currentUser = cupaUserService.getUserWithAuthorities()
                .map(CupaUser.class::cast)
                .orElse(null);
            
            if (currentUser == null) {
                LOG.warn("No current user found when loading merchants for user form");
                return List.of();
            }
            
            // Load merchants with pagination (get first 1000)
            Pageable pageable = PageRequest.of(0, 1000);
            return merchantService.findAllWithAccessControl(pageable, currentUser).getContent();
        } catch (Exception e) {
            LOG.error("Error loading merchants for user form", e);
            return List.of();
        }
    }
    
    /**
     * Converter between Set of MerchantDTOs (UI component value) and 
     * comma-separated String (entity field value).
     */
    private class MerchantSelectionConverter implements Converter<Set<MerchantDTO>, String> {
        
        @Override
        public Result<String> convertToModel(Set<MerchantDTO> selectedMerchants, ValueContext context) {
            if (selectedMerchants == null || selectedMerchants.isEmpty()) {
                return Result.ok("");
            }
            
            // Convert Set<MerchantDTO> to comma-separated merchant IDs
            String merchantIds = selectedMerchants.stream()
                .map(MerchantDTO::getId)
                .sorted()
                .collect(Collectors.joining(","));
            
            return Result.ok(merchantIds);
        }
        
        @Override
        public Set<MerchantDTO> convertToPresentation(String merchantIds, ValueContext context) {
            if (merchantIds == null || merchantIds.trim().isEmpty()) {
                return Set.of();
            }
            
            // Parse comma-separated merchant IDs
            Set<String> idSet = Arrays.stream(merchantIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toSet());
            
            // Match against available merchants in the combobox
            if (merchantComboBox != null && merchantComboBox.getListDataView() != null) {
                Set<MerchantDTO> selectedMerchants = new HashSet<>();
                merchantComboBox.getListDataView().getItems().forEach(merchant -> {
                    if (idSet.contains(merchant.getId())) {
                        selectedMerchants.add(merchant);
                    }
                });
                return selectedMerchants;
            }
            
            return Set.of();
        }
    }
}

