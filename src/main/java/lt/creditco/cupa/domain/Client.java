package lt.creditco.cupa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents the end-user (customer) of a merchant.
 * This entity is based on the Client object described in the
 * \"My Gateway Product Services Guide\".
 */
@Entity
@Table(name = "client")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "merchant_client_id", nullable = false)
    private String merchantClientId;

    @Column(name = "name")
    private String name;

    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "client_phone")
    private String clientPhone;

    @Column(name = "valid")
    private Boolean valid;

    @Column(name = "street_number")
    private String streetNumber;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "street_suffix")
    private String streetSuffix;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "country")
    private String country;

    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted;

    @Column(name = "is_correlated_blacklisted")
    private Boolean isCorrelatedBlacklisted;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "client")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "client" }, allowSetters = true)
    private Set<ClientCard> cards = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "clients", "transactions", "auditLogs" }, allowSetters = true)
    private Merchant merchant;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Client id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMerchantClientId() {
        return this.merchantClientId;
    }

    public Client merchantClientId(String merchantClientId) {
        this.setMerchantClientId(merchantClientId);
        return this;
    }

    public void setMerchantClientId(String merchantClientId) {
        this.merchantClientId = merchantClientId;
    }

    public String getName() {
        return this.name;
    }

    public Client name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public Client emailAddress(String emailAddress) {
        this.setEmailAddress(emailAddress);
        return this;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public Client mobileNumber(String mobileNumber) {
        this.setMobileNumber(mobileNumber);
        return this;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getClientPhone() {
        return this.clientPhone;
    }

    public Client clientPhone(String clientPhone) {
        this.setClientPhone(clientPhone);
        return this;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public Boolean getValid() {
        return this.valid;
    }

    public Client valid(Boolean valid) {
        this.setValid(valid);
        return this;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getStreetNumber() {
        return this.streetNumber;
    }

    public Client streetNumber(String streetNumber) {
        this.setStreetNumber(streetNumber);
        return this;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return this.streetName;
    }

    public Client streetName(String streetName) {
        this.setStreetName(streetName);
        return this;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetSuffix() {
        return this.streetSuffix;
    }

    public Client streetSuffix(String streetSuffix) {
        this.setStreetSuffix(streetSuffix);
        return this;
    }

    public void setStreetSuffix(String streetSuffix) {
        this.streetSuffix = streetSuffix;
    }

    public String getCity() {
        return this.city;
    }

    public Client city(String city) {
        this.setCity(city);
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return this.state;
    }

    public Client state(String state) {
        this.setState(state);
        return this;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public Client postCode(String postCode) {
        this.setPostCode(postCode);
        return this;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return this.country;
    }

    public Client country(String country) {
        this.setCountry(country);
        return this;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getIsBlacklisted() {
        return this.isBlacklisted;
    }

    public Client isBlacklisted(Boolean isBlacklisted) {
        this.setIsBlacklisted(isBlacklisted);
        return this;
    }

    public void setIsBlacklisted(Boolean isBlacklisted) {
        this.isBlacklisted = isBlacklisted;
    }

    public Boolean getIsCorrelatedBlacklisted() {
        return this.isCorrelatedBlacklisted;
    }

    public Client isCorrelatedBlacklisted(Boolean isCorrelatedBlacklisted) {
        this.setIsCorrelatedBlacklisted(isCorrelatedBlacklisted);
        return this;
    }

    public void setIsCorrelatedBlacklisted(Boolean isCorrelatedBlacklisted) {
        this.isCorrelatedBlacklisted = isCorrelatedBlacklisted;
    }

    public Set<ClientCard> getCards() {
        return this.cards;
    }

    public void setCards(Set<ClientCard> clientCards) {
        if (this.cards != null) {
            this.cards.forEach(i -> i.setClient(null));
        }
        if (clientCards != null) {
            clientCards.forEach(i -> i.setClient(this));
        }
        this.cards = clientCards;
    }

    public Client cards(Set<ClientCard> clientCards) {
        this.setCards(clientCards);
        return this;
    }

    public Client addCard(ClientCard clientCard) {
        this.cards.add(clientCard);
        clientCard.setClient(this);
        return this;
    }

    public Client removeCard(ClientCard clientCard) {
        this.cards.remove(clientCard);
        clientCard.setClient(null);
        return this;
    }

    public Merchant getMerchant() {
        return this.merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Client merchant(Merchant merchant) {
        this.setMerchant(merchant);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Client)) {
            return false;
        }
        return getId() != null && getId().equals(((Client) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Client{" +
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
            "}";
    }
}
