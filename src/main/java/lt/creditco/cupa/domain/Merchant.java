package lt.creditco.cupa.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;

/**
 * Represents a merchant using the CUPA system.
 * It stores all the necessary configuration and credentials
 * for both our system (CUPA) and the upstream remote gateway.
 */
@Entity
@Table(name = "merchant")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Merchant extends AbstractAuditingEntity<String> implements MerchantOwnedEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @NaturalId
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private MerchantMode mode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MerchantStatus status;

    @Column(name = "balance", precision = 21, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    private Currency currency;

    @Column(name = "cupa_test_api_key")
    private String cupaTestApiKey;

    @Column(name = "cupa_prod_api_key")
    private String cupaProdApiKey;

    @Column(name = "remote_test_url")
    private String remoteTestUrl;

    @Column(name = "remote_test_merchant_id")
    private String remoteTestMerchantId;

    @Column(name = "remote_test_merchant_key")
    private String remoteTestMerchantKey;

    @Column(name = "remote_test_api_key")
    private String remoteTestApiKey;

    @Column(name = "remote_prod_url")
    private String remoteProdUrl;

    @Column(name = "remote_prod_merchant_id")
    private String remoteProdMerchantId;

    @Column(name = "remote_prod_merchant_key")
    private String remoteProdMerchantKey;

    @Column(name = "remote_prod_api_key")
    private String remoteProdApiKey;

    @Version
    private Long version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Merchant id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Merchant name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MerchantMode getMode() {
        return this.mode;
    }

    public Merchant mode(MerchantMode mode) {
        this.setMode(mode);
        return this;
    }

    public void setMode(MerchantMode mode) {
        this.mode = mode;
    }

    public MerchantStatus getStatus() {
        return this.status;
    }

    public Merchant status(MerchantStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(MerchantStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public Merchant balance(BigDecimal balance) {
        this.setBalance(balance);
        return this;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public Merchant currency(Currency currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCupaTestApiKey() {
        return this.cupaTestApiKey;
    }

    public Merchant cupaTestApiKey(String cupaTestApiKey) {
        this.setCupaTestApiKey(cupaTestApiKey);
        return this;
    }

    public void setCupaTestApiKey(String cupaTestApiKey) {
        this.cupaTestApiKey = cupaTestApiKey;
    }

    public String getCupaProdApiKey() {
        return this.cupaProdApiKey;
    }

    public Merchant cupaProdApiKey(String cupaProdApiKey) {
        this.setCupaProdApiKey(cupaProdApiKey);
        return this;
    }

    public void setCupaProdApiKey(String cupaProdApiKey) {
        this.cupaProdApiKey = cupaProdApiKey;
    }

    public String getRemoteTestUrl() {
        return this.remoteTestUrl;
    }

    public Merchant remoteTestUrl(String remoteTestUrl) {
        this.setRemoteTestUrl(remoteTestUrl);
        return this;
    }

    public void setRemoteTestUrl(String remoteTestUrl) {
        this.remoteTestUrl = remoteTestUrl;
    }

    public String getRemoteTestMerchantId() {
        return this.remoteTestMerchantId;
    }

    public Merchant remoteTestMerchantId(String remoteTestMerchantId) {
        this.setRemoteTestMerchantId(remoteTestMerchantId);
        return this;
    }

    public void setRemoteTestMerchantId(String remoteTestMerchantId) {
        this.remoteTestMerchantId = remoteTestMerchantId;
    }

    public String getRemoteTestMerchantKey() {
        return this.remoteTestMerchantKey;
    }

    public Merchant remoteTestMerchantKey(String remoteTestMerchantKey) {
        this.setRemoteTestMerchantKey(remoteTestMerchantKey);
        return this;
    }

    public void setRemoteTestMerchantKey(String remoteTestMerchantKey) {
        this.remoteTestMerchantKey = remoteTestMerchantKey;
    }

    public String getRemoteTestApiKey() {
        return this.remoteTestApiKey;
    }

    public Merchant remoteTestApiKey(String remoteTestApiKey) {
        this.setRemoteTestApiKey(remoteTestApiKey);
        return this;
    }

    public void setRemoteTestApiKey(String remoteTestApiKey) {
        this.remoteTestApiKey = remoteTestApiKey;
    }

    public String getRemoteProdUrl() {
        return this.remoteProdUrl;
    }

    public Merchant remoteProdUrl(String remoteProdUrl) {
        this.setRemoteProdUrl(remoteProdUrl);
        return this;
    }

    public void setRemoteProdUrl(String remoteProdUrl) {
        this.remoteProdUrl = remoteProdUrl;
    }

    public String getRemoteProdMerchantId() {
        return this.remoteProdMerchantId;
    }

    public Merchant remoteProdMerchantId(String remoteProdMerchantId) {
        this.setRemoteProdMerchantId(remoteProdMerchantId);
        return this;
    }

    public void setRemoteProdMerchantId(String remoteProdMerchantId) {
        this.remoteProdMerchantId = remoteProdMerchantId;
    }

    public String getRemoteProdMerchantKey() {
        return this.remoteProdMerchantKey;
    }

    public Merchant remoteProdMerchantKey(String remoteProdMerchantKey) {
        this.setRemoteProdMerchantKey(remoteProdMerchantKey);
        return this;
    }

    public void setRemoteProdMerchantKey(String remoteProdMerchantKey) {
        this.remoteProdMerchantKey = remoteProdMerchantKey;
    }

    public String getRemoteProdApiKey() {
        return this.remoteProdApiKey;
    }

    public Merchant remoteProdApiKey(String remoteProdApiKey) {
        this.setRemoteProdApiKey(remoteProdApiKey);
        return this;
    }

    public void setRemoteProdApiKey(String remoteProdApiKey) {
        this.remoteProdApiKey = remoteProdApiKey;
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
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Merchant)) {
            return false;
        }
        return getId() != null && getId().equals(((Merchant) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Merchant{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", mode='" + getMode() + "'" +
            ", status='" + getStatus() + "'" +
            ", balance=" + getBalance() +
            ", currency='" + getCurrency() + "'" +
            ", cupaTestApiKey='" + getCupaTestApiKey() + "'" +
            ", cupaProdApiKey='" + getCupaProdApiKey() + "'" +
            ", remoteTestUrl='" + getRemoteTestUrl() + "'" +
            ", remoteTestMerchantId='" + getRemoteTestMerchantId() + "'" +
            ", remoteTestMerchantKey='" + getRemoteTestMerchantKey() + "'" +
            ", remoteTestApiKey='" + getRemoteTestApiKey() + "'" +
            ", remoteProdUrl='" + getRemoteProdUrl() + "'" +
            ", remoteProdMerchantId='" + getRemoteProdMerchantId() + "'" +
            ", remoteProdMerchantKey='" + getRemoteProdMerchantKey() + "'" +
            ", remoteProdApiKey='" + getRemoteProdApiKey() + "'" +
            "}";
    }

    /**
     * Get the merchant key for signature verification.
     * This method retrieves the merchant key from the merchant context.
     *
     * @return the merchant key, or null if not found
     */
    public String getMerchantKeyByMode() {
        String merchantKey;
        if (getMode() == lt.creditco.cupa.domain.enumeration.MerchantMode.LIVE) {
            merchantKey = getRemoteProdMerchantKey();
        } else {
            merchantKey = getRemoteTestMerchantKey();
        }
        if (StringUtils.isBlank(merchantKey)) return null;

        return merchantKey;
    }
}
