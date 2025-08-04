package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
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
public class PaymentTransactionDTO implements Serializable {

    private Long id;

    @NotNull
    private String orderId;

    @NotNull
    private UUID cupaTransactionId;

    private String gatewayTransactionId;

    @NotNull
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

    private Boolean sendEmail;

    private String signature;

    private String signatureVersion;

    @NotNull
    private Instant requestTimestamp;

    @Lob
    private String requestData;

    @Lob
    private String initialResponseData;

    private Instant callbackTimestamp;

    @Lob
    private String callbackData;

    @Lob
    private String lastQueryData;

    private ClientDTO client;

    @NotNull
    private MerchantDTO merchant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public UUID getCupaTransactionId() {
        return cupaTransactionId;
    }

    public void setCupaTransactionId(UUID cupaTransactionId) {
        this.cupaTransactionId = cupaTransactionId;
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

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
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

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public MerchantDTO getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantDTO merchant) {
        this.merchant = merchant;
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
            ", cupaTransactionId='" + getCupaTransactionId() + "'" +
            ", gatewayTransactionId='" + getGatewayTransactionId() + "'" +
            ", status='" + getStatus() + "'" +
            ", statusDescription='" + getStatusDescription() + "'" +
            ", paymentBrand='" + getPaymentBrand() + "'" +
            ", amount=" + getAmount() +
            ", balance=" + getBalance() +
            ", currency='" + getCurrency() + "'" +
            ", replyUrl='" + getReplyUrl() + "'" +
            ", backofficeUrl='" + getBackofficeUrl() + "'" +
            ", echo='" + getEcho() + "'" +
            ", sendEmail='" + getSendEmail() + "'" +
            ", signature='" + getSignature() + "'" +
            ", signatureVersion='" + getSignatureVersion() + "'" +
            ", requestTimestamp='" + getRequestTimestamp() + "'" +
            ", requestData='" + getRequestData() + "'" +
            ", initialResponseData='" + getInitialResponseData() + "'" +
            ", callbackTimestamp='" + getCallbackTimestamp() + "'" +
            ", callbackData='" + getCallbackData() + "'" +
            ", lastQueryData='" + getLastQueryData() + "'" +
            ", client=" + getClient() +
            ", merchant=" + getMerchant() +
            "}";
    }
}
