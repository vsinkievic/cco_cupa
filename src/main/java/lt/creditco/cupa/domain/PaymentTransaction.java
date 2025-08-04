package lt.creditco.cupa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.PaymentBrand;
import lt.creditco.cupa.domain.enumeration.TransactionStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a single payment transaction from start to finish.
 * This entity acts as a comprehensive audit log, storing the request,
 * response, and status at each step of the process.
 */
@Entity
@Table(name = "payment_transaction")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PaymentTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @NotNull
    @Column(name = "cupa_transaction_id", nullable = false)
    private UUID cupaTransactionId;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "status_description")
    private String statusDescription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_brand", nullable = false)
    private PaymentBrand paymentBrand;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance", precision = 21, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;

    @Column(name = "reply_url")
    private String replyUrl;

    @Column(name = "backoffice_url")
    private String backofficeUrl;

    @Column(name = "echo")
    private String echo;

    @Column(name = "send_email")
    private Boolean sendEmail;

    @Column(name = "signature")
    private String signature;

    @Column(name = "signature_version")
    private String signatureVersion;

    @NotNull
    @Column(name = "request_timestamp", nullable = false)
    private Instant requestTimestamp;

    @Lob
    @Column(name = "request_data")
    private String requestData;

    @Lob
    @Column(name = "initial_response_data")
    private String initialResponseData;

    @Column(name = "callback_timestamp")
    private Instant callbackTimestamp;

    @Lob
    @Column(name = "callback_data")
    private String callbackData;

    @Lob
    @Column(name = "last_query_data")
    private String lastQueryData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "cards", "merchant" }, allowSetters = true)
    private Client client;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "clients", "transactions", "auditLogs" }, allowSetters = true)
    private Merchant merchant;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PaymentTransaction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public PaymentTransaction orderId(String orderId) {
        this.setOrderId(orderId);
        return this;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public UUID getCupaTransactionId() {
        return this.cupaTransactionId;
    }

    public PaymentTransaction cupaTransactionId(UUID cupaTransactionId) {
        this.setCupaTransactionId(cupaTransactionId);
        return this;
    }

    public void setCupaTransactionId(UUID cupaTransactionId) {
        this.cupaTransactionId = cupaTransactionId;
    }

    public String getGatewayTransactionId() {
        return this.gatewayTransactionId;
    }

    public PaymentTransaction gatewayTransactionId(String gatewayTransactionId) {
        this.setGatewayTransactionId(gatewayTransactionId);
        return this;
    }

    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public TransactionStatus getStatus() {
        return this.status;
    }

    public PaymentTransaction status(TransactionStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return this.statusDescription;
    }

    public PaymentTransaction statusDescription(String statusDescription) {
        this.setStatusDescription(statusDescription);
        return this;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public PaymentBrand getPaymentBrand() {
        return this.paymentBrand;
    }

    public PaymentTransaction paymentBrand(PaymentBrand paymentBrand) {
        this.setPaymentBrand(paymentBrand);
        return this;
    }

    public void setPaymentBrand(PaymentBrand paymentBrand) {
        this.paymentBrand = paymentBrand;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public PaymentTransaction amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public PaymentTransaction balance(BigDecimal balance) {
        this.setBalance(balance);
        return this;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public PaymentTransaction currency(Currency currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getReplyUrl() {
        return this.replyUrl;
    }

    public PaymentTransaction replyUrl(String replyUrl) {
        this.setReplyUrl(replyUrl);
        return this;
    }

    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
    }

    public String getBackofficeUrl() {
        return this.backofficeUrl;
    }

    public PaymentTransaction backofficeUrl(String backofficeUrl) {
        this.setBackofficeUrl(backofficeUrl);
        return this;
    }

    public void setBackofficeUrl(String backofficeUrl) {
        this.backofficeUrl = backofficeUrl;
    }

    public String getEcho() {
        return this.echo;
    }

    public PaymentTransaction echo(String echo) {
        this.setEcho(echo);
        return this;
    }

    public void setEcho(String echo) {
        this.echo = echo;
    }

    public Boolean getSendEmail() {
        return this.sendEmail;
    }

    public PaymentTransaction sendEmail(Boolean sendEmail) {
        this.setSendEmail(sendEmail);
        return this;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getSignature() {
        return this.signature;
    }

    public PaymentTransaction signature(String signature) {
        this.setSignature(signature);
        return this;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignatureVersion() {
        return this.signatureVersion;
    }

    public PaymentTransaction signatureVersion(String signatureVersion) {
        this.setSignatureVersion(signatureVersion);
        return this;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public Instant getRequestTimestamp() {
        return this.requestTimestamp;
    }

    public PaymentTransaction requestTimestamp(Instant requestTimestamp) {
        this.setRequestTimestamp(requestTimestamp);
        return this;
    }

    public void setRequestTimestamp(Instant requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public PaymentTransaction requestData(String requestData) {
        this.setRequestData(requestData);
        return this;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getInitialResponseData() {
        return this.initialResponseData;
    }

    public PaymentTransaction initialResponseData(String initialResponseData) {
        this.setInitialResponseData(initialResponseData);
        return this;
    }

    public void setInitialResponseData(String initialResponseData) {
        this.initialResponseData = initialResponseData;
    }

    public Instant getCallbackTimestamp() {
        return this.callbackTimestamp;
    }

    public PaymentTransaction callbackTimestamp(Instant callbackTimestamp) {
        this.setCallbackTimestamp(callbackTimestamp);
        return this;
    }

    public void setCallbackTimestamp(Instant callbackTimestamp) {
        this.callbackTimestamp = callbackTimestamp;
    }

    public String getCallbackData() {
        return this.callbackData;
    }

    public PaymentTransaction callbackData(String callbackData) {
        this.setCallbackData(callbackData);
        return this;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }

    public String getLastQueryData() {
        return this.lastQueryData;
    }

    public PaymentTransaction lastQueryData(String lastQueryData) {
        this.setLastQueryData(lastQueryData);
        return this;
    }

    public void setLastQueryData(String lastQueryData) {
        this.lastQueryData = lastQueryData;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public PaymentTransaction client(Client client) {
        this.setClient(client);
        return this;
    }

    public Merchant getMerchant() {
        return this.merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public PaymentTransaction merchant(Merchant merchant) {
        this.setMerchant(merchant);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentTransaction)) {
            return false;
        }
        return getId() != null && getId().equals(((PaymentTransaction) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PaymentTransaction{" +
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
            "}";
    }
}
