package lt.creditco.cupa.ui.paymenttransaction.excel;

import com.bpmid.excel_exporter.ExcelCell;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;

/**
 * Row model for Excel export of payment transactions (annotation-driven {@link com.bpmid.excel_exporter.ExcelExporter}).
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentTransactionExportData {


    @ExcelCell(name = "Merchant ID", width = 18, format = "@")
    private String merchantId;

    @ExcelCell(name = "Order ID", width = 30, format = "@")
    private String orderId;

    @ExcelCell(name = "Request Timestamp", width = 22)
    private Instant requestTimestamp;

    @ExcelCell(name = "Amount", width = 12, format = "#,##0.00")
    private BigDecimal amount;

    @ExcelCell(name = "Currency", width = 10)
    private Currency currency;

    @ExcelCell(name = "Status", width = 12)
    private TransactionStatus status;

    @ExcelCell(name = "Status Description", width = 40, format = "@")
    private String statusDescription;

//    @ExcelCell(name = "Balance", width = 15, format = "#,##0.00")
//    private BigDecimal balance;


    @ExcelCell(name = "Payment Brand", width = 16)
    private PaymentBrand paymentBrand;

    @ExcelCell(name = "Payment Flow", width = 12)
    private PaymentFlow paymentFlow;

    @ExcelCell(name = "Merchant Client ID", width = 22, format = "@")
    private String merchantClientId;

    @ExcelCell(name = "Client Name", width = 30, format = "@")
    private String clientName;

    @ExcelCell(name = "Client Email", width = 30, format = "@")
    private String clientEmail;

    @ExcelCell(name = "Merchant Name", width = 30, format = "@")
    private String merchantName;

    @ExcelCell(name = "Environment", width = 12)
    private MerchantMode environment;


    @ExcelCell(name = "ID", width = 28, format = "@")
    private String id;

    @ExcelCell(name = "Client ID", width = 22, format = "@")
    private String clientId;

//    @ExcelCell(name = "Gateway Txn ID", width = 22, format = "@")
//    private String gatewayTransactionId;


    public PaymentTransactionExportData(PaymentTransactionDTO dto) {
        this.id = dto.getId();
        this.orderId = dto.getOrderId();
//        this.gatewayTransactionId = dto.getGatewayTransactionId();
        this.status = dto.getStatus();
        this.statusDescription = dto.getStatusDescription();
        this.paymentBrand = dto.getPaymentBrand();
        this.amount = dto.getAmount();
//        this.balance = dto.getBalance();
        this.currency = dto.getCurrency();
        this.paymentFlow = dto.getPaymentFlow();
        this.requestTimestamp = dto.getRequestTimestamp();
        this.clientId = dto.getClientId();
        this.merchantClientId = dto.getMerchantClientId();
        this.merchantId = dto.getMerchantId();
        this.clientName = dto.getClientName();
        this.clientEmail = dto.getClientEmail();
        this.merchantName = dto.getMerchantName();
        this.environment = dto.getEnvironment();
    }
}
