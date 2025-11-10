package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lt.creditco.cupa.api.PaymentFlow;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.PaymentTransaction} entity.
 */
@Schema(
    description = "Represents a single payment transaction from start to finish.\nThis entity acts as a comprehensive audit log, storing the request,\nresponse, and status at each step of the process."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PaymentTransactionDTO implements Serializable, MerchantOwnedEntity {

    private String id;

    @NotNull
    private String orderId;

    private String gatewayTransactionId;

    private TransactionStatus status;

    private String statusDescription;

    @NotNull
    private PaymentBrand paymentBrand;

    @NotNull
    private BigDecimal amount;

    private BigDecimal balance;

    @NotNull
    private Currency currency;

    private String replyUrl;

    private String backofficeUrl;

    private String echo;

    private PaymentFlow paymentFlow;

    private String signature;

    private String signatureVersion;

    private Instant requestTimestamp;

    private String requestData;

    private String initialResponseData;

    private Instant callbackTimestamp;

    private String callbackData;

    private String lastQueryData;

    private String clientId;

    private String merchantClientId;

    private String merchantId;

    private String clientName;

    private String merchantName;

    private Long version;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public PaymentBrand getPaymentBrand() {
        return paymentBrand;
    }

    public void setPaymentBrand(PaymentBrand paymentBrand) {
        this.paymentBrand = paymentBrand;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getReplyUrl() {
        return replyUrl;
    }

    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
    }

    public String getBackofficeUrl() {
        return backofficeUrl;
    }

    public void setBackofficeUrl(String backofficeUrl) {
        this.backofficeUrl = backofficeUrl;
    }

    public String getEcho() {
        return echo;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public PaymentFlow getPaymentFlow() {
        return paymentFlow;
    }

    public void setPaymentFlow(PaymentFlow paymentFlow) {
        this.paymentFlow = paymentFlow;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Instant requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getInitialResponseData() {
        return initialResponseData;
    }

    public void setInitialResponseData(String initialResponseData) {
        this.initialResponseData = initialResponseData;
    }

    public Instant getCallbackTimestamp() {
        return callbackTimestamp;
    }

    public void setCallbackTimestamp(Instant callbackTimestamp) {
        this.callbackTimestamp = callbackTimestamp;
    }

    public String getCallbackData() {
        return callbackData;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }

    public String getLastQueryData() {
        return lastQueryData;
    }

    public void setLastQueryData(String lastQueryData) {
        this.lastQueryData = lastQueryData;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMerchantClientId() {
        return merchantClientId;
    }

    public void setMerchantClientId(String merchantClientId) {
        this.merchantClientId = merchantClientId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return this.version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentTransactionDTO)) {
            return false;
        }

        PaymentTransactionDTO paymentTransactionDTO = (PaymentTransactionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, paymentTransactionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PaymentTransactionDTO{" +
            "id=" + getId() +
            ", orderId='" + getOrderId() + "'" +
            ", clientId='" + getClientId() + "'" +
            ", merchantId='" + getMerchantId() + "'" +
            ", clientName='" + getClientName() + "'" +
            ", merchantName='" + getMerchantName() + "'" +
            ", gatewayTransactionId='" + getGatewayTransactionId() + "'" +
            ", status='" + getStatus() + "'" +
            ", statusDescription='" + getStatusDescription() + "'" +
            ", paymentBrand='" + getPaymentBrand() + "'" +
            ", amount=" + getAmount() +
            ", balance=" + getBalance() +
            ", currency='" + getCurrency() + "'" +
            ", paymentFlow='" + getPaymentFlow() + "'" +
            ", requestTimestamp='" + getRequestTimestamp() + "'" +
            "}";
    }
}
