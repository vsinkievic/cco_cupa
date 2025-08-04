package lt.creditco.cupa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a merchant using the CUPA system.
 * It stores all the necessary configuration and credentials
 * for both our system (CUPA) and the upstream remote gateway.
 */
@Entity
@Table(name = "merchant")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Merchant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "cards", "merchant" }, allowSetters = true)
    private Set<Client> clients = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "client", "merchant" }, allowSetters = true)
    private Set<PaymentTransaction> transactions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "merchant")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "merchant" }, allowSetters = true)
    private Set<AuditLog> auditLogs = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Merchant id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
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

    public Set<Client> getClients() {
        return this.clients;
    }

    public void setClients(Set<Client> clients) {
        if (this.clients != null) {
            this.clients.forEach(i -> i.setMerchant(null));
        }
        if (clients != null) {
            clients.forEach(i -> i.setMerchant(this));
        }
        this.clients = clients;
    }

    public Merchant clients(Set<Client> clients) {
        this.setClients(clients);
        return this;
    }

    public Merchant addClient(Client client) {
        this.clients.add(client);
        client.setMerchant(this);
        return this;
    }

    public Merchant removeClient(Client client) {
        this.clients.remove(client);
        client.setMerchant(null);
        return this;
    }

    public Set<PaymentTransaction> getTransactions() {
        return this.transactions;
    }

    public void setTransactions(Set<PaymentTransaction> paymentTransactions) {
        if (this.transactions != null) {
            this.transactions.forEach(i -> i.setMerchant(null));
        }
        if (paymentTransactions != null) {
            paymentTransactions.forEach(i -> i.setMerchant(this));
        }
        this.transactions = paymentTransactions;
    }

    public Merchant transactions(Set<PaymentTransaction> paymentTransactions) {
        this.setTransactions(paymentTransactions);
        return this;
    }

    public Merchant addTransaction(PaymentTransaction paymentTransaction) {
        this.transactions.add(paymentTransaction);
        paymentTransaction.setMerchant(this);
        return this;
    }

    public Merchant removeTransaction(PaymentTransaction paymentTransaction) {
        this.transactions.remove(paymentTransaction);
        paymentTransaction.setMerchant(null);
        return this;
    }

    public Set<AuditLog> getAuditLogs() {
        return this.auditLogs;
    }

    public void setAuditLogs(Set<AuditLog> auditLogs) {
        if (this.auditLogs != null) {
            this.auditLogs.forEach(i -> i.setMerchant(null));
        }
        if (auditLogs != null) {
            auditLogs.forEach(i -> i.setMerchant(this));
        }
        this.auditLogs = auditLogs;
    }

    public Merchant auditLogs(Set<AuditLog> auditLogs) {
        this.setAuditLogs(auditLogs);
        return this;
    }

    public Merchant addAuditLog(AuditLog auditLog) {
        this.auditLogs.add(auditLog);
        auditLog.setMerchant(this);
        return this;
    }

    public Merchant removeAuditLog(AuditLog auditLog) {
        this.auditLogs.remove(auditLog);
        auditLog.setMerchant(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
}
