package lt.creditco.cupa.ui.components;

import com.bpmid.vapp.base.ui.components.VappDatePicker;
import com.bpmid.vapp.service.UserService;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import lt.creditco.cupa.domain.DailyAmountLimit;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Custom Vaadin field component for editing DailyAmountLimit.
 * 
 * <p>This component provides a user-friendly interface for configuring daily monetary transaction limits
 * with linear interpolation between two dates.</p>
 */
public class DailyAmountLimitField extends CustomField<DailyAmountLimit> {

    private final VappDatePicker startDateField;
    private final BigDecimalField startAmountField = new BigDecimalField("Start Amount");
    private final VappDatePicker afterDateField;
    private final BigDecimalField afterAmountField = new BigDecimalField("After Amount");

    public DailyAmountLimitField(UserService userService) {
        this(userService, "");
    }

    public DailyAmountLimitField(UserService userService, String label) {
        this.startDateField = new VappDatePicker(userService, "Start Date");
        this.afterDateField = new VappDatePicker(userService, "After Date");
        setLabel(label);
        
        // Configure amount fields
        startAmountField.setHelperText("Monetary amount (must be >= 0)");
        afterAmountField.setHelperText("Monetary amount (must be >= 0)");

        // Set width
        startDateField.setWidthFull();
        startAmountField.setWidthFull();
        afterDateField.setWidthFull();
        afterAmountField.setWidthFull();

        // Create layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        formLayout.add(startDateField, startAmountField);
        formLayout.add(afterDateField, afterAmountField);

        // Add helper text explaining the concept
        formLayout.getElement().setAttribute("title", 
            "Configure daily transaction limits with linear interpolation. " +
            "Leave all fields empty for no limit. " +
            "Set only 'After' fields to start from 0. " +
            "Set all fields for gradual increase from start to after date.");

        add(formLayout);

        // Listen for value changes to update the model
        startDateField.addValueChangeListener(e -> updateValue());
        startAmountField.addValueChangeListener(e -> updateValue());
        afterDateField.addValueChangeListener(e -> updateValue());
        afterAmountField.addValueChangeListener(e -> updateValue());
    }

    @Override
    protected DailyAmountLimit generateModelValue() {
        LocalDate startDate = startDateField.getValue();
        BigDecimal startAmount = startAmountField.getValue();
        LocalDate afterDate = afterDateField.getValue();
        BigDecimal afterAmount = afterAmountField.getValue();

        // If all fields are empty, return null
        if (startDate == null && startAmount == null && afterDate == null && afterAmount == null) {
            return null;
        }

        // Create and populate the limit object
        DailyAmountLimit limit = new DailyAmountLimit();
        limit.setStartDate(startDate);
        limit.setStartAmount(startAmount);
        limit.setAfterDate(afterDate);
        limit.setAfterAmount(afterAmount);

        return limit;
    }

    @Override
    protected void setPresentationValue(DailyAmountLimit limit) {
        if (limit == null) {
            startDateField.clear();
            startAmountField.clear();
            afterDateField.clear();
            afterAmountField.clear();
        } else {
            startDateField.setValue(limit.getStartDate());
            startAmountField.setValue(limit.getStartAmount());
            afterDateField.setValue(limit.getAfterDate());
            afterAmountField.setValue(limit.getAfterAmount());
        }
    }

    /**
     * Sets the read-only state for all internal fields.
     * This method ensures the component respects the form mode (VIEW/EDIT/NEW).
     *
     * @param readOnly true to make all fields read-only, false to make them editable
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        startDateField.setReadOnly(readOnly);
        startAmountField.setReadOnly(readOnly);
        afterDateField.setReadOnly(readOnly);
        afterAmountField.setReadOnly(readOnly);
    }
}

