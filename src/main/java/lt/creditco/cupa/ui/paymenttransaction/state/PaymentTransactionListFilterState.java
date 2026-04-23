package lt.creditco.cupa.ui.paymenttransaction.state;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lt.creditco.cupa.application.PaymentTransactionListDatePreset;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Session-persisted filter state for the payment transaction list (survives navigation to detail and back).
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PaymentTransactionListFilterState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderIdFilter = "";
    /** Enum name, or null when not filtering by brand. */
    private String paymentBrandName;
    private String amountFilter = "";
    /** {@link lt.creditco.cupa.domain.enumeration.TransactionStatus} name, or null. */
    private String statusName;
    private String merchantId;
    /** {@link lt.creditco.cupa.domain.enumeration.MerchantMode} name, or null. */
    private String environmentName;

    private String datePresetName = PaymentTransactionListDatePreset.defaultPeriod().name();
    private LocalDate fromDate;
    private LocalDate toDate;

    public String getOrderIdFilter() {
        return orderIdFilter;
    }

    public void setOrderIdFilter(String orderIdFilter) {
        this.orderIdFilter = orderIdFilter == null ? "" : orderIdFilter;
    }

    public String getPaymentBrandName() {
        return paymentBrandName;
    }

    public void setPaymentBrandName(String paymentBrandName) {
        this.paymentBrandName = paymentBrandName;
    }

    public String getAmountFilter() {
        return amountFilter;
    }

    public void setAmountFilter(String amountFilter) {
        this.amountFilter = amountFilter == null ? "" : amountFilter;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getDatePresetName() {
        return datePresetName;
    }

    public void setDatePresetName(String datePresetName) {
        this.datePresetName = datePresetName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public void reset() {
        orderIdFilter = "";
        paymentBrandName = null;
        amountFilter = "";
        statusName = null;
        merchantId = null;
        environmentName = null;
        datePresetName = PaymentTransactionListDatePreset.defaultPeriod().name();
        fromDate = null;
        toDate = null;
    }
}
