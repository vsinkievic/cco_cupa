package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.AuditLog} entity.
 */
@Schema(description = "A generic entity to log every API request for audit and reporting.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AuditLogDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant requestTimestamp;

    @NotNull
    private String apiEndpoint;

    @NotNull
    private String httpMethod;

    private Integer httpStatusCode;

    private String orderId;

    private String responseDescription;

    private String cupaApiKey;

    private String environment;

    @Lob
    private String requestData;

    @Lob
    private String responseData;

    private String requesterIpAddress;

    private MerchantDTO merchant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Instant requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getCupaApiKey() {
        return cupaApiKey;
    }

    public void setCupaApiKey(String cupaApiKey) {
        this.cupaApiKey = cupaApiKey;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getRequesterIpAddress() {
        return requesterIpAddress;
    }

    public void setRequesterIpAddress(String requesterIpAddress) {
        this.requesterIpAddress = requesterIpAddress;
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
        if (!(o instanceof AuditLogDTO)) {
            return false;
        }

        AuditLogDTO auditLogDTO = (AuditLogDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, auditLogDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditLogDTO{" +
            "id=" + getId() +
            ", requestTimestamp='" + getRequestTimestamp() + "'" +
            ", apiEndpoint='" + getApiEndpoint() + "'" +
            ", httpMethod='" + getHttpMethod() + "'" +
            ", httpStatusCode=" + getHttpStatusCode() +
            ", orderId='" + getOrderId() + "'" +
            ", responseDescription='" + getResponseDescription() + "'" +
            ", cupaApiKey='" + getCupaApiKey() + "'" +
            ", environment='" + getEnvironment() + "'" +
            ", requestData='" + getRequestData() + "'" +
            ", responseData='" + getResponseData() + "'" +
            ", requesterIpAddress='" + getRequesterIpAddress() + "'" +
            ", merchant=" + getMerchant() +
            "}";
    }
}
