package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.ClientCard} entity.
 */
@Schema(
    description = "Represents a client's stored payment card.\nBased on the 'cards' object in the documentation.\nPAN is stored masked for security."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientCardDTO implements Serializable {

    private String id;

    @NotNull
    private String maskedPan;

    private String expiryDate;

    private String cardholderName;

    private Boolean isDefault;

    private Boolean isValid;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Long version;

    @NotNull
    private ClientDTO client;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientCardDTO)) {
            return false;
        }

        ClientCardDTO clientCardDTO = (ClientCardDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, clientCardDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClientCardDTO{" +
            "id=" + getId() +
            ", maskedPan='" + getMaskedPan() + "'" +
            ", expiryDate='" + getExpiryDate() + "'" +
            ", cardholderName='" + getCardholderName() + "'" +
            ", isDefault='" + getIsDefault() + "'" +
            ", isValid='" + getIsValid() + "'" +
            ", client=" + getClient() +
            "}";
    }
}
