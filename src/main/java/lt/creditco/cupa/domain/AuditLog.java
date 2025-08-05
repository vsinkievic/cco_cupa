package lt.creditco.cupa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A generic entity to log every API request for audit and reporting.
 */
@Entity
@Table(name = "audit_log")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditLog extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "request_timestamp", nullable = false)
    private Instant requestTimestamp;

    @NotNull
    @Column(name = "api_endpoint", nullable = false)
    private String apiEndpoint;

    @NotNull
    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "response_description")
    private String responseDescription;

    @Column(name = "cupa_api_key")
    private String cupaApiKey;

    @Column(name = "environment")
    private String environment;

    @Lob
    @Column(name = "request_data")
    private String requestData;

    @Lob
    @Column(name = "response_data")
    private String responseData;

    @Column(name = "requester_ip_address")
    private String requesterIpAddress;

    @Column(name = "merchant_id")
    private String merchantId;

    @Version
    private Long version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuditLog id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRequestTimestamp() {
        return this.requestTimestamp;
    }

    public AuditLog requestTimestamp(Instant requestTimestamp) {
        this.setRequestTimestamp(requestTimestamp);
        return this;
    }

    public void setRequestTimestamp(Instant requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getApiEndpoint() {
        return this.apiEndpoint;
    }

    public AuditLog apiEndpoint(String apiEndpoint) {
        this.setApiEndpoint(apiEndpoint);
        return this;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public AuditLog httpMethod(String httpMethod) {
        this.setHttpMethod(httpMethod);
        return this;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getHttpStatusCode() {
        return this.httpStatusCode;
    }

    public AuditLog httpStatusCode(Integer httpStatusCode) {
        this.setHttpStatusCode(httpStatusCode);
        return this;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public AuditLog orderId(String orderId) {
        this.setOrderId(orderId);
        return this;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getResponseDescription() {
        return this.responseDescription;
    }

    public AuditLog responseDescription(String responseDescription) {
        this.setResponseDescription(responseDescription);
        return this;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getCupaApiKey() {
        return this.cupaApiKey;
    }

    public AuditLog cupaApiKey(String cupaApiKey) {
        this.setCupaApiKey(cupaApiKey);
        return this;
    }

    public void setCupaApiKey(String cupaApiKey) {
        this.cupaApiKey = cupaApiKey;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public AuditLog environment(String environment) {
        this.setEnvironment(environment);
        return this;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public AuditLog requestData(String requestData) {
        this.setRequestData(requestData);
        return this;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public AuditLog responseData(String responseData) {
        this.setResponseData(responseData);
        return this;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getRequesterIpAddress() {
        return this.requesterIpAddress;
    }

    public AuditLog requesterIpAddress(String requesterIpAddress) {
        this.setRequesterIpAddress(requesterIpAddress);
        return this;
    }

    public void setRequesterIpAddress(String requesterIpAddress) {
        this.requesterIpAddress = requesterIpAddress;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public AuditLog merchantId(String merchantId) {
        this.setMerchantId(merchantId);
        return this;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditLog)) {
            return false;
        }
        return getId() != null && getId().equals(((AuditLog) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditLog{" +
            "id=" + getId() +
            ", requestTimestamp='" + getRequestTimestamp() + "'" +
            ", apiEndpoint='" + getApiEndpoint() + "'" +
            ", httpMethod='" + getHttpMethod() + "'" +
            ", httpStatusCode=" + getHttpStatusCode() +
            ", merchantId='" + getMerchantId() + "'" +
            ", orderId='" + getOrderId() + "'" +
            ", responseDescription='" + getResponseDescription() + "'" +
            ", cupaApiKey='" + getCupaApiKey() + "'" +
            ", environment='" + getEnvironment() + "'" +
            ", requestData='" + getRequestData() + "'" +
            ", responseData='" + getResponseData() + "'" +
            ", requesterIpAddress='" + getRequesterIpAddress() + "'" +
            "}";
    }
}
