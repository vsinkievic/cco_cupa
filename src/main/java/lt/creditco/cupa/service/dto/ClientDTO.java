package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.Client} entity.
 */
@Schema(
    description = "Represents the end-user (customer) of a merchant.\nThis entity is based on the Client object described in the\n\"My Gateway Product Services Guide\"."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientDTO implements Serializable {

    private String id;

    private String merchantClientId;

    private String gatewayClientId;

    private String name;

    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private String emailAddress;

    private String mobileNumber;

    private String clientPhone;

    private Boolean valid;

    private String streetNumber;

    private String streetName;

    private String streetSuffix;

    private String city;

    private String state;

    private String postCode;

    private String country;

    private Boolean isBlacklisted;

    private Boolean isCorrelatedBlacklisted;

    @NotNull
    private String merchantId;

    private String merchantName;

    private Long version;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Instant createdInGateway;

    private Instant updatedInGateway;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMerchantClientId() {
        return merchantClientId;
    }

    public void setMerchantClientId(String merchantClientId) {
        this.merchantClientId = merchantClientId;
    }

    public String getGatewayClientId() {
        return gatewayClientId;
    }

    public void setGatewayClientId(String gatewayClientId) {
        this.gatewayClientId = gatewayClientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetSuffix() {
        return streetSuffix;
    }

    public void setStreetSuffix(String streetSuffix) {
        this.streetSuffix = streetSuffix;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getIsBlacklisted() {
        return isBlacklisted;
    }

    public void setIsBlacklisted(Boolean isBlacklisted) {
        this.isBlacklisted = isBlacklisted;
    }

    public Boolean getIsCorrelatedBlacklisted() {
        return isCorrelatedBlacklisted;
    }

    public void setIsCorrelatedBlacklisted(Boolean isCorrelatedBlacklisted) {
        this.isCorrelatedBlacklisted = isCorrelatedBlacklisted;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return this.merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public Instant getCreatedInGateway() {
        return createdInGateway;
    }

    public void setCreatedInGateway(Instant createdInGateway) {
        this.createdInGateway = createdInGateway;
    }

    public Instant getUpdatedInGateway() {
        return updatedInGateway;
    }

    public void setUpdatedInGateway(Instant updatedInGateway) {
        this.updatedInGateway = updatedInGateway;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientDTO)) {
            return false;
        }

        ClientDTO clientDTO = (ClientDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, clientDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClientDTO{" +
            "id=" + getId() +
            ", merchanId='" + getMerchantId() + "'" +
            ", name='" + getName() + "'" +
            ", emailAddress='" + getEmailAddress() + "'" +
            ", mobileNumber='" + getMobileNumber() + "'" +
            ", clientPhone='" + getClientPhone() + "'" +
            ", valid='" + getValid() + "'" +
            ", streetNumber='" + getStreetNumber() + "'" +
            ", streetName='" + getStreetName() + "'" +
            ", streetSuffix='" + getStreetSuffix() + "'" +
            ", city='" + getCity() + "'" +
            ", state='" + getState() + "'" +
            ", postCode='" + getPostCode() + "'" +
            ", country='" + getCountry() + "'" +
            ", isBlacklisted='" + getIsBlacklisted() + "'" +
            ", isCorrelatedBlacklisted='" + getIsCorrelatedBlacklisted() + "'" +
            "}";
    }
}
