package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
import com.bpmid.vapp.base.ui.components.VappDatePicker;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.application.PaymentTransactionListDatePreset;
import lt.creditco.cupa.application.PaymentTransactionListDatePresets;
import lt.creditco.cupa.util.UserLocalDateRange;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Scope;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
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
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final CupaUser loggedInUser;
    private final Grid<PaymentTransactionDTO> grid = new Grid<>(PaymentTransactionDTO.class, false);
    private Grid.Column<PaymentTransactionDTO> timestampColumn;

    // Filters
    private final TextField orderIdFilter = new TextField("Order ID");
    private final ComboBox<PaymentBrand> paymentBrandFilter = new ComboBox<>("Payment Brand");
    private final TextField amountFilter = new TextField("Amount");
    private final ComboBox<TransactionStatus> statusFilter = new ComboBox<>("Status");
    private final ComboBox<MerchantDTO> merchantFilter = new ComboBox<>("Merchant");
    private final ComboBox<MerchantMode> environmentFilter = new ComboBox<>("Environment");

    private final ComboBox<PaymentTransactionListDatePreset> periodFilter = new ComboBox<>("Date range");
    private final VappDatePicker fromDate;
    private final VappDatePicker toDate;

    /** When true, date field updates come from a preset, not the user, so the period is not set to custom. */
    private boolean syncingPeriodToDates = false;

    public PaymentTransactionListView(
        PaymentTransactionService paymentTransactionService,
        MerchantService merchantService,
        CupaUserService cupaUserService
    ) {
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService
            .getUserWithAuthorities()
            .map(CupaUser.class::cast)
            .orElseThrow(() -> new RuntimeException("User not found"));
        this.paymentTransactionService = paymentTransactionService;
        this.merchantService = merchantService;

        this.fromDate = new VappDatePicker(cupaUserService, "From");
        this.toDate = new VappDatePicker(cupaUserService, "To");

        setSizeFull();
        setPadding(true);

        BreadcrumbBar breadcrumbBar = new BreadcrumbBar(
            Breadcrumbs.builder().home().currentLink("Payment Transactions", PaymentTransactionListView.class).build()
        );

        add(breadcrumbBar, createToolbar(), createGrid());

        loadMerchants();
        initDateRangeAndRefresh();
    }

    private void initDateRangeAndRefresh() {
        periodFilter.setItems(orderedPeriods());
        periodFilter.setItemLabelGenerator(PaymentTransactionListDatePreset::getLabel);
        periodFilter.setClearButtonVisible(true);

        syncingPeriodToDates = true;
        periodFilter.setValue(PaymentTransactionListDatePreset.defaultPeriod());
        applyCurrentPeriodToDatePickers();
        syncingPeriodToDates = false;

        fromDate.addValueChangeListener(e -> onDateChangedByUser());
        toDate.addValueChangeListener(e -> onDateChangedByUser());
        periodFilter.addValueChangeListener(e -> onPeriodValueChanged());
        fromDate.setClearButtonVisible(true);
        toDate.setClearButtonVisible(true);

        refreshGrid();
    }

    private static List<PaymentTransactionListDatePreset> orderedPeriods() {
        return Arrays.asList(
            PaymentTransactionListDatePreset.TODAY,
            PaymentTransactionListDatePreset.THIS_WEEK,
            PaymentTransactionListDatePreset.LAST_WEEK,
            PaymentTransactionListDatePreset.THIS_MONTH,
            PaymentTransactionListDatePreset.LAST_MONTH,
            PaymentTransactionListDatePreset.SINCE_START_PREV_MONTH,
            PaymentTransactionListDatePreset.THIS_YEAR,
            PaymentTransactionListDatePreset.LAST_YEAR,
            PaymentTransactionListDatePreset.CUSTOM
        );
    }

    private void onPeriodValueChanged() {
        if (syncingPeriodToDates) {
            return;
        }
        PaymentTransactionListDatePreset p = periodFilter.getValue();
        if (p == null || p == PaymentTransactionListDatePreset.CUSTOM) {
            refreshGrid();
            return;
        }
        syncingPeriodToDates = true;
        try {
            applyCurrentPeriodToDatePickers();
        } finally {
            syncingPeriodToDates = false;
        }
        refreshGrid();
    }

    private void onDateChangedByUser() {
        if (syncingPeriodToDates) {
            return;
        }
        syncingPeriodToDates = true;
        try {
            periodFilter.setValue(PaymentTransactionListDatePreset.CUSTOM);
        } finally {
            syncingPeriodToDates = false;
        }
        refreshGrid();
    }

    private void applyCurrentPeriodToDatePickers() {
        PaymentTransactionListDatePreset p = periodFilter.getValue();
        if (p == null || p == PaymentTransactionListDatePreset.CUSTOM) {
            return;
        }
        ZoneId z = cupaUserService.getCurrentUserTimezone();
        int firstDow = cupaUserService.getCurrentUserFirstDayOfWeek();
        LocalDate[] r = PaymentTransactionListDatePresets.resolveLocalDates(p, z, firstDow);
        fromDate.setValue(r[0]);
        toDate.setValue(r[1]);
    }

    private void loadMerchants() {
        log.debug("Loading merchants for user: {}", loggedInUser.getLogin());
        var merchants = merchantService.findAllWithAccessControl(PageRequest.of(0, 100), loggedInUser).getContent();
        merchantFilter.setItems(merchants);
        merchantFilter.setItemLabelGenerator(merchant -> merchant.getId() + " - " + merchant.getName());
    }

    private VerticalLayout createToolbar() {
        // Order ID filter (exact match)
        orderIdFilter.setPlaceholder("Order ID");
        orderIdFilter.setClearButtonVisible(true);
        orderIdFilter.setValueChangeMode(ValueChangeMode.LAZY);
        orderIdFilter.addValueChangeListener(e -> refreshGrid());

        // Payment Brand filter
        paymentBrandFilter.setPlaceholder("All brands");
        paymentBrandFilter.setClearButtonVisible(true);
        paymentBrandFilter.setItems(PaymentBrand.values());
        paymentBrandFilter.addValueChangeListener(e -> refreshGrid());

        // Amount filter (exact match)
        amountFilter.setPlaceholder("Amount");
        amountFilter.setClearButtonVisible(true);
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);
        amountFilter.addValueChangeListener(e -> refreshGrid());

        // Status filter
        statusFilter.setPlaceholder("All statuses");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setItems(TransactionStatus.values());
        statusFilter.addValueChangeListener(e -> refreshGrid());

        // Merchant filter
        merchantFilter.setPlaceholder("All merchants");
        merchantFilter.setClearButtonVisible(true);
        merchantFilter.addValueChangeListener(e -> refreshGrid());

        // Environment filter
        environmentFilter.setPlaceholder("All environments");
        environmentFilter.setClearButtonVisible(true);
        environmentFilter.setItems(MerchantMode.values());
        environmentFilter.addValueChangeListener(e -> refreshGrid());

        Button createButton = new Button("New Transaction", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(PaymentTransactionCreateView.class)));

        HorizontalLayout firstRow = new HorizontalLayout(
            orderIdFilter,
            paymentBrandFilter,
            amountFilter,
            statusFilter,
            merchantFilter,
            environmentFilter,
            createButton
        );
        firstRow.setDefaultVerticalComponentAlignment(Alignment.END);
        firstRow.setWidthFull();
        firstRow.expand(orderIdFilter);

        HorizontalLayout secondRow = new HorizontalLayout(periodFilter, fromDate, toDate);
        secondRow.setDefaultVerticalComponentAlignment(Alignment.END);
        secondRow.setWidthFull();
        secondRow.expand(periodFilter);

        VerticalLayout toolbars = new VerticalLayout(firstRow, secondRow);
        toolbars.setPadding(false);
        toolbars.setSpacing(true);
        toolbars.setWidthFull();
        return toolbars;
    }

    private Grid<PaymentTransactionDTO> createGrid() {
        grid.addColumn(PaymentTransactionDTO::getOrderId).setHeader("Order ID").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getPaymentBrand).setHeader("Payment Brand").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getAmount).setHeader("Amount").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getCurrency).setHeader("Currency").setSortable(true).setAutoWidth(true);
        timestampColumn = grid
            .addColumn(PaymentTransactionDTO::getRequestTimestamp)
            .setHeader("Timestamp")
            .setSortable(true)
            .setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getStatus).setHeader("Status").setSortable(true).setAutoWidth(true);
        grid.addColumn(PaymentTransactionDTO::getEnvironment).setHeader("Env").setSortable(true).setWidth("80px");

        grid
            .addComponentColumn(tx -> {
                RouterLink viewLink = new RouterLink("", PaymentTransactionDetailView.class, tx.getId());
                viewLink.add(VaadinIcon.EYE.create());
                viewLink.getElement().setAttribute("title", "View Transaction");

                RouterLink cloneLink = new RouterLink("", PaymentTransactionCreateView.class);
                cloneLink.add(VaadinIcon.COPY.create());
                cloneLink.getElement().setAttribute("title", "Clone payment");
                cloneLink.setQueryParameters(
                    new com.vaadin.flow.router.QueryParameters(java.util.Map.of("cloneFrom", java.util.List.of(tx.getId())))
                );

                HorizontalLayout actions = new HorizontalLayout(viewLink, cloneLink);
                actions.setSpacing(true);
                return actions;
            })
            .setHeader(" ")
            .setWidth("100px")
            .setFlexGrow(0);

        grid.addItemDoubleClickListener(event ->
            getUI().ifPresent(ui -> ui.navigate(PaymentTransactionDetailView.class, event.getItem().getId()))
        );

        grid.setSizeFull();
        return grid;
    }

    private void refreshGrid() {
        log.debug("Refreshing payment transactions grid for user: {}", loggedInUser.getLogin());

        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (from == null || to == null) {
            applyEmptyGridWithFilters();
            return;
        }

        ZoneId zone = cupaUserService.getCurrentUserTimezone();
        Instant startInclusive = UserLocalDateRange.startInclusiveAtZoneStartOfDay(from, zone);
        Instant endExclusive = UserLocalDateRange.endExclusiveAfterToDate(to, zone);
        if (!startInclusive.isBefore(endExclusive)) {
            // e.g. "from" after "to" with no extra validation: empty result
            applyEmptyGridWithFilters();
            return;
        }

        var pageable = PageRequest.of(0, 1000);
        List<PaymentTransactionDTO> allTransactions = paymentTransactionService
            .findAllWithAccessControl(pageable, loggedInUser, startInclusive, endExclusive)
            .getContent();

        ListDataProvider<PaymentTransactionDTO> dataProvider = new ListDataProvider<>(allTransactions);
        dataProvider.setFilter(this::rowMatchesFieldFilters);
        grid.setDataProvider(dataProvider);
        sortGridByTimestamp();
    }

    private void applyEmptyGridWithFilters() {
        ListDataProvider<PaymentTransactionDTO> dataProvider = new ListDataProvider<>(Collections.emptyList());
        dataProvider.setFilter(this::rowMatchesFieldFilters);
        grid.setDataProvider(dataProvider);
        sortGridByTimestamp();
    }

    private boolean rowMatchesFieldFilters(PaymentTransactionDTO tx) {
        String orderIdValue = orderIdFilter.getValue();
        PaymentBrand paymentBrandValue = paymentBrandFilter.getValue();
        String amountValue = amountFilter.getValue();
        TransactionStatus statusValue = statusFilter.getValue();
        MerchantDTO merchantValue = merchantFilter.getValue();
        MerchantMode envValue = environmentFilter.getValue();

        if (orderIdValue != null && !orderIdValue.isEmpty() && (tx.getOrderId() == null || !tx.getOrderId().equals(orderIdValue))) {
            return false;
        }

        if (paymentBrandValue != null && tx.getPaymentBrand() != null && !tx.getPaymentBrand().equals(paymentBrandValue)) {
            return false;
        }

        if (amountValue != null && !amountValue.isEmpty()) {
            try {
                BigDecimal filterAmount = new BigDecimal(amountValue);
                if (tx.getAmount() == null || tx.getAmount().compareTo(filterAmount) != 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                log.debug("Invalid amount filter value: {}", amountValue);
            }
        }

        if (statusValue != null && tx.getStatus() != null && !tx.getStatus().equals(statusValue)) {
            return false;
        }

        if (merchantValue != null && tx.getMerchantId() != null && !tx.getMerchantId().equals(merchantValue.getId())) {
            return false;
        }

        if (envValue != null) {
            if (tx.getEnvironment() == null) {
                return false;
            }
            if (!tx.getEnvironment().equals(envValue)) {
                return false;
            }
        }

        return true;
    }

    private void sortGridByTimestamp() {
        grid.sort(
            java.util.Collections.singletonList(new GridSortOrder<>(timestampColumn, SortDirection.DESCENDING))
        );
    }
}
