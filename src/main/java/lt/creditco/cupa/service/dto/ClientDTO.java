package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.Client} entity.
 */
@Schema(
    description = "Represents the end-user (customer) of a merchant.\nThis entity is based on the Client object described in the\n\"My Gateway Product Services Guide\"."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientDTO implements Serializable {

    private Long id;

    @NotNull
    private String merchantClientId;

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
    private MerchantDTO merchant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchantClientId() {
        return merchantClientId;
    }

    public void setMerchantClientId(String merchantClientId) {
        this.merchantClientId = merchantClientId;
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
            ", merchantClientId='" + getMerchantClientId() + "'" +
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
            ", merchant=" + getMerchant() +
            "}";
    }
}
