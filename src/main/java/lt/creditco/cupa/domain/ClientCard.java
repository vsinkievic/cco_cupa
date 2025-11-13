package lt.creditco.cupa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a client's stored payment card.
 * Based on the 'cards' object in the documentation.
 * PAN is stored masked for security.
 */
@Entity
@Table(name = "client_card")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ClientCard extends AbstractAuditingEntity<String> implements MerchantOwnedEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "masked_pan", nullable = false)
    private String maskedPan;

    @Column(name = "expiry_date")
    private String expiryDate;

    @Column(name = "cardholder_name")
    private String cardholderName;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "is_valid")
    private Boolean isValid;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "cards", "merchant" }, allowSetters = true)
    private Client client;

    @Version
    private Long version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public ClientCard id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id.toString();
    }

    public void setId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    public void id(String id) {
        this.id = id;
    }

    public String getMaskedPan() {
        return this.maskedPan;
    }

    public ClientCard maskedPan(String maskedPan) {
        this.setMaskedPan(maskedPan);
        return this;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public ClientCard expiryDate(String expiryDate) {
        this.setExpiryDate(expiryDate);
        return this;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCardholderName() {
        return this.cardholderName;
    }

    public ClientCard cardholderName(String cardholderName) {
        this.setCardholderName(cardholderName);
        return this;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public Boolean getIsDefault() {
        return this.isDefault;
    }

    public ClientCard isDefault(Boolean isDefault) {
        this.setIsDefault(isDefault);
        return this;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsValid() {
        return this.isValid;
    }

    public ClientCard isValid(Boolean isValid) {
        this.setIsValid(isValid);
        return this;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ClientCard client(Client client) {
        this.setClient(client);
        return this;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public String getMerchantId() {
        return client != null ? client.getMerchantId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientCard)) {
            return false;
        }
        return getId() != null && getId().equals(((ClientCard) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ClientCard{" +
            "id=" + getId() +
            ", maskedPan='" + getMaskedPan() + "'" +
            ", expiryDate='" + getExpiryDate() + "'" +
            ", cardholderName='" + getCardholderName() + "'" +
            ", isDefault='" + getIsDefault() + "'" +
            ", isValid='" + getIsValid() + "'" +
            "}";
    }
}
