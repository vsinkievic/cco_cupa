package lt.creditco.cupa.ui.paymenttransaction;

import com.bpmid.vapp.base.ui.MainLayout;
import com.bpmid.vapp.base.ui.breadcrumb.*;
import com.bpmid.vapp.base.ui.components.VappDatePicker;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lt.creditco.cupa.application.PaymentTransactionListDatePreset;
import lt.creditco.cupa.application.PaymentTransactionListDatePresets;
import lt.creditco.cupa.base.users.CupaUser;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.security.AuthoritiesConstants;
import lt.creditco.cupa.service.CupaUserService;
import lt.creditco.cupa.service.MerchantService;
import lt.creditco.cupa.service.PaymentTransactionService;
import lt.creditco.cupa.service.dto.MerchantDTO;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import lt.creditco.cupa.ui.paymenttransaction.excel.PaymentTransactionExcelExporter;
import lt.creditco.cupa.ui.paymenttransaction.state.PaymentTransactionListFilterState;
import lt.creditco.cupa.util.UserLocalDateRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;

/**
 * Vaadin view for listing Payment Transactions.
 */
@Route(value = "payment-transactions", layout = MainLayout.class)
@PageTitle("Payment Transactions | CUPA")
@Scope("prototype")
@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREDITCO, AuthoritiesConstants.MERCHANT, AuthoritiesConstants.USER })
@Slf4j
public class PaymentTransactionListView extends VerticalLayout {

    private static final int LIST_FETCH_LIMIT = 10_000;
    private static final DateTimeFormatter EXCEL_NAME_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final PaymentTransactionService paymentTransactionService;
    private final MerchantService merchantService;
    private final CupaUserService cupaUserService;
    private final PaymentTransactionListFilterState filterState;
    private final PaymentTransactionExcelExporter excelExporter;
    private final CupaUser loggedInUser;
    private final Grid<PaymentTransactionDTO> grid = new Grid<>(PaymentTransactionDTO.class, false);
    private Grid.Column<PaymentTransactionDTO> timestampColumn;
    private final Span countLabel = new Span();

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
        CupaUserService cupaUserService,
        PaymentTransactionListFilterState filterState,
        PaymentTransactionExcelExporter excelExporter
    ) {
        this.cupaUserService = cupaUserService;
        this.loggedInUser = cupaUserService
            .getUserWithAuthorities()
            .map(CupaUser.class::cast)
            .orElseThrow(() -> new RuntimeException("User not found"));
        this.paymentTransactionService = paymentTransactionService;
        this.merchantService = merchantService;
        this.filterState = filterState;
        this.excelExporter = excelExporter;

        this.fromDate = new VappDatePicker(cupaUserService, "From");
        this.toDate = new VappDatePicker(cupaUserService, "To");

        setSizeFull();
        setPadding(true);

        periodFilter.setItems(orderedPeriods());
        periodFilter.setItemLabelGenerator(PaymentTransactionListDatePreset::getLabel);
        periodFilter.setClearButtonVisible(true);
        fromDate.setClearButtonVisible(true);
        toDate.setClearButtonVisible(true);

        BreadcrumbBar breadcrumbBar = new BreadcrumbBar(
            Breadcrumbs.builder().home().currentLink("Payment Transactions", PaymentTransactionListView.class).build()
        );

        countLabel.getStyle().set("margin-left", "auto");

        add(breadcrumbBar, createHeader(), createToolbar(), createGrid());

        loadMerchants();
        initializeFiltersFromState();
        initDateRangeAndRefresh();
        attachFieldFilterListeners();
    }

    private void initializeFiltersFromState() {
        orderIdFilter.setValue(filterState.getOrderIdFilter());
        amountFilter.setValue(filterState.getAmountFilter());
        paymentBrandFilter.setValue(parseEnum(PaymentBrand.class, filterState.getPaymentBrandName()));
        statusFilter.setValue(parseEnum(TransactionStatus.class, filterState.getStatusName()));
        environmentFilter.setValue(parseEnum(MerchantMode.class, filterState.getEnvironmentName()));

        PaymentTransactionListDatePreset period = parseEnum(PaymentTransactionListDatePreset.class, filterState.getDatePresetName());
        periodFilter.setValue(period != null ? period : PaymentTransactionListDatePreset.defaultPeriod());

        if (filterState.getFromDate() != null) {
            fromDate.setValue(filterState.getFromDate());
        } else {
            fromDate.clear();
        }
        if (filterState.getToDate() != null) {
            toDate.setValue(filterState.getToDate());
        } else {
            toDate.clear();
        }
        if (filterState.getMerchantId() != null) {
            merchantFilter
                .getListDataView()
                .getItems()
                .filter(m -> filterState.getMerchantId().equals(m.getId()))
                .findFirst()
                .ifPresent(merchantFilter::setValue);
        } else {
            merchantFilter.clear();
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String name) {
        if (name == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void persistStateFromUi() {
        filterState.setOrderIdFilter(orderIdFilter.getValue());
        filterState.setAmountFilter(amountFilter.getValue());
        filterState.setPaymentBrandName(paymentBrandFilter.getValue() == null ? null : paymentBrandFilter.getValue().name());
        filterState.setStatusName(statusFilter.getValue() == null ? null : statusFilter.getValue().name());
        filterState.setEnvironmentName(environmentFilter.getValue() == null ? null : environmentFilter.getValue().name());
        filterState.setMerchantId(merchantFilter.getValue() == null ? null : merchantFilter.getValue().getId());
        if (periodFilter.getValue() != null) {
            filterState.setDatePresetName(periodFilter.getValue().name());
        }
        filterState.setFromDate(fromDate.getValue());
        filterState.setToDate(toDate.getValue());
    }

    private void clearFilters() {
        filterState.reset();
        initializeFiltersFromState();
        syncingPeriodToDates = true;
        try {
            if (fromDate.getValue() == null && toDate.getValue() == null) {
                applyCurrentPeriodToDatePickers();
            }
        } finally {
            syncingPeriodToDates = false;
        }
        refreshGrid();
    }

    private Component createHeader() {
        H2 title = new H2("Payment Transactions");
        Anchor exportExcelAnchor = createExcelExportAnchor();
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> refreshGrid());

        HorizontalLayout header = new HorizontalLayout(title, exportExcelAnchor, countLabel, refreshButton);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.expand(title);
        return header;
    }

    private Anchor createExcelExportAnchor() {
        Anchor exportExcelAnchor = new Anchor();
        exportExcelAnchor.getElement().setAttribute("download", true);
        exportExcelAnchor.setTarget("_blank");

        Button exportExcelButton = new Button("Export to Excel");
        exportExcelButton.addClickListener(event -> {
            try {
                List<PaymentTransactionDTO> visibleRecords = grid.getListDataView().getItems().toList();
                if (visibleRecords.isEmpty()) {
                    Notification.show("No records to export", 3000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
                    return;
                }
                String fileName = "payment-transactions-" + LocalDateTime.now().format(EXCEL_NAME_TS) + ".xlsx";
                byte[] excelData = excelExporter.exportToExcel(visibleRecords, cupaUserService.getCurrentUserTimezone());
                exportExcelAnchor.setHref(
                    "data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64," +
                    java.util.Base64.getEncoder().encodeToString(excelData)
                );
                exportExcelAnchor.getElement().setAttribute("download", fileName);
                exportExcelAnchor.getElement().callJsFunction("click");
                Notification.show(
                    String.format("Exported %d record%s to Excel", visibleRecords.size(), visibleRecords.size() == 1 ? "" : "s"),
                    3000,
                    Notification.Position.TOP_END
                )
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                log.error("Failed to export to Excel", e);
                Notification.show("Export failed: " + e.getMessage(), 5000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        exportExcelAnchor.add(exportExcelButton);
        return exportExcelAnchor;
    }

    private void initDateRangeAndRefresh() {
        syncingPeriodToDates = true;
        try {
            if (fromDate.getValue() == null && toDate.getValue() == null) {
                applyCurrentPeriodToDatePickers();
            }
        } finally {
            syncingPeriodToDates = false;
        }

        fromDate.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                onDateChangedByUser();
            }
        });
        toDate.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                onDateChangedByUser();
            }
        });
        periodFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                onPeriodValueChanged();
            }
        });

        refreshGrid();
    }

    private void attachFieldFilterListeners() {
        orderIdFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
        paymentBrandFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
        amountFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
        statusFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
        merchantFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
        environmentFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refreshGrid();
            }
        });
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

        // Payment Brand filter
        paymentBrandFilter.setPlaceholder("All brands");
        paymentBrandFilter.setClearButtonVisible(true);
        paymentBrandFilter.setItems(PaymentBrand.values());

        // Amount filter (exact match)
        amountFilter.setPlaceholder("Amount");
        amountFilter.setClearButtonVisible(true);
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);

        // Status filter
        statusFilter.setPlaceholder("All statuses");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setItems(TransactionStatus.values());

        // Merchant filter
        merchantFilter.setPlaceholder("All merchants");
        merchantFilter.setClearButtonVisible(true);

        // Environment filter
        environmentFilter.setPlaceholder("All environments");
        environmentFilter.setClearButtonVisible(true);
        environmentFilter.setItems(MerchantMode.values());

        Button clearButton = new Button("Clear", VaadinIcon.CLOSE.create());
        clearButton.addClickListener(e -> clearFilters());

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
            clearButton,
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
        persistStateFromUi();
        log.debug("Refreshing payment transactions grid for user: {}", loggedInUser.getLogin());

        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (from == null || to == null) {
            countLabel.setText("Set From and To dates to load transactions");
            countLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
            applyEmptyGridWithFilters();
            return;
        }

        ZoneId zone = cupaUserService.getCurrentUserTimezone();
        Instant startInclusive = UserLocalDateRange.startInclusiveAtZoneStartOfDay(from, zone);
        Instant endExclusive = UserLocalDateRange.endExclusiveAfterToDate(to, zone);
        if (!startInclusive.isBefore(endExclusive)) {
            countLabel.setText("Invalid date range (From must be before To)");
            countLabel.getStyle().set("color", "var(--lumo-error-text-color)");
            applyEmptyGridWithFilters();
            return;
        }

        List<PaymentTransactionDTO> allTransactions = paymentTransactionService.findListWithAccessControl(
            loggedInUser,
            startInclusive,
            endExclusive,
            LIST_FETCH_LIMIT
        );

        ListDataProvider<PaymentTransactionDTO> dataProvider = new ListDataProvider<>(allTransactions);
        dataProvider.setFilter(this::rowMatchesFieldFilters);
        grid.setDataProvider(dataProvider);
        sortGridByTimestamp();
        int visible = (int) grid.getListDataView().getItems().count();
        updateCountLabel(visible, allTransactions.size() >= LIST_FETCH_LIMIT);
    }

    private void updateCountLabel(int visibleSize, boolean hitServerLimit) {
        if (visibleSize == 0) {
            countLabel.setText("No payment transactions to display (check date range and field filters)");
            countLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        } else if (hitServerLimit) {
            countLabel.setText(
                String.format(
                    "Showing %,d matching row%s (capped at %,d from server; more data may exist)",
                    visibleSize,
                    visibleSize == 1 ? "" : "s",
                    LIST_FETCH_LIMIT
                )
            );
            countLabel.getStyle().set("color", "var(--lumo-warning-text-color)");
        } else {
            countLabel.setText(
                String.format("Showing %,d payment transaction%s", visibleSize, visibleSize == 1 ? "" : "s")
            );
            countLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        }
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

        if (paymentBrandValue != null && (tx.getPaymentBrand() == null || !tx.getPaymentBrand().equals(paymentBrandValue))) {
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

        if (statusValue != null && (tx.getStatus() == null || !tx.getStatus().equals(statusValue))) {
            return false;
        }

        if (merchantValue != null && (tx.getMerchantId() == null || !tx.getMerchantId().equals(merchantValue.getId()))) {
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
