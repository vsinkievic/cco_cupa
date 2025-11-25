package lt.creditco.cupa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import lt.creditco.cupa.domain.DailyAmountLimit;
import lt.creditco.cupa.domain.enumeration.Currency;
import lt.creditco.cupa.domain.enumeration.MerchantMode;
import lt.creditco.cupa.domain.enumeration.MerchantStatus;

/**
 * A DTO for the {@link lt.creditco.cupa.domain.Merchant} entity.
 */
@Schema(
    description = "Represents a merchant using the CUPA system.\nIt stores all the necessary configuration and credentials\nfor both our system (CUPA) and the upstream remote gateway."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MerchantDTO implements Serializable {

    private String id;

    @NotNull
    private String name;

    @NotNull
    private MerchantMode mode;

    @NotNull
    private MerchantStatus status;

    private BigDecimal balance;

    private Currency currency;

    private String cupaTestApiKey;

    private String cupaProdApiKey;

    private String remoteTestUrl;

    private String remoteTestMerchantId;

    private String remoteTestMerchantKey;

    private String remoteTestApiKey;

    private String remoteProdUrl;

    private String remoteProdMerchantId;

    private String remoteProdMerchantKey;

    private String remoteProdApiKey;

    private String testClientIdPrefix;

    private String testOrderIdPrefix;

    private String liveClientIdPrefix;

    private String liveOrderIdPrefix;

    private DailyAmountLimit testDailyAmountLimit;

    private DailyAmountLimit liveDailyAmountLimit;

    private Long version;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MerchantMode getMode() {
        return mode;
    }

    public void setMode(MerchantMode mode) {
        this.mode = mode;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public void setStatus(MerchantStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getCupaTestApiKey() {
        return cupaTestApiKey;
    }

    public void setCupaTestApiKey(String cupaTestApiKey) {
        this.cupaTestApiKey = cupaTestApiKey;
    }

    public String getCupaProdApiKey() {
        return cupaProdApiKey;
    }

    public void setCupaProdApiKey(String cupaProdApiKey) {
        this.cupaProdApiKey = cupaProdApiKey;
    }

    public String getRemoteTestUrl() {
        return remoteTestUrl;
    }

    public void setRemoteTestUrl(String remoteTestUrl) {
        this.remoteTestUrl = remoteTestUrl;
    }

    public String getRemoteTestMerchantId() {
        return remoteTestMerchantId;
    }

    public void setRemoteTestMerchantId(String remoteTestMerchantId) {
        this.remoteTestMerchantId = remoteTestMerchantId;
    }

    public String getRemoteTestMerchantKey() {
        return remoteTestMerchantKey;
    }

    public void setRemoteTestMerchantKey(String remoteTestMerchantKey) {
        this.remoteTestMerchantKey = remoteTestMerchantKey;
    }

    public String getRemoteTestApiKey() {
        return remoteTestApiKey;
    }

    public void setRemoteTestApiKey(String remoteTestApiKey) {
        this.remoteTestApiKey = remoteTestApiKey;
    }

    public String getRemoteProdUrl() {
        return remoteProdUrl;
    }

    public void setRemoteProdUrl(String remoteProdUrl) {
        this.remoteProdUrl = remoteProdUrl;
    }

    public String getRemoteProdMerchantId() {
        return remoteProdMerchantId;
    }

    public void setRemoteProdMerchantId(String remoteProdMerchantId) {
        this.remoteProdMerchantId = remoteProdMerchantId;
    }

    public String getRemoteProdMerchantKey() {
        return remoteProdMerchantKey;
    }

    public void setRemoteProdMerchantKey(String remoteProdMerchantKey) {
        this.remoteProdMerchantKey = remoteProdMerchantKey;
    }

    public String getRemoteProdApiKey() {
        return remoteProdApiKey;
    }

    public void setRemoteProdApiKey(String remoteProdApiKey) {
        this.remoteProdApiKey = remoteProdApiKey;
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
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getTestClientIdPrefix() {
        return testClientIdPrefix;
    }

    public void setTestClientIdPrefix(String testClientIdPrefix) {
        this.testClientIdPrefix = testClientIdPrefix;
    }

    public String getTestOrderIdPrefix() {
        return testOrderIdPrefix;
    }

    public void setTestOrderIdPrefix(String testOrderIdPrefix) {
        this.testOrderIdPrefix = testOrderIdPrefix;
    }

    public String getLiveClientIdPrefix() {
        return liveClientIdPrefix;
    }

    public void setLiveClientIdPrefix(String liveClientIdPrefix) {
        this.liveClientIdPrefix = liveClientIdPrefix;
    }

    public String getLiveOrderIdPrefix() {
        return liveOrderIdPrefix;
    }

    public void setLiveOrderIdPrefix(String liveOrderIdPrefix) {
        this.liveOrderIdPrefix = liveOrderIdPrefix;
    }

    public DailyAmountLimit getTestDailyAmountLimit() {
        return testDailyAmountLimit;
    }

    public void setTestDailyAmountLimit(DailyAmountLimit testDailyAmountLimit) {
        this.testDailyAmountLimit = testDailyAmountLimit;
    }

    public DailyAmountLimit getLiveDailyAmountLimit() {
        return liveDailyAmountLimit;
    }

    public void setLiveDailyAmountLimit(DailyAmountLimit liveDailyAmountLimit) {
        this.liveDailyAmountLimit = liveDailyAmountLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MerchantDTO)) {
            return false;
        }

        MerchantDTO merchantDTO = (MerchantDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, merchantDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MerchantDTO{" +
            "id=" + getId() +
            ", version=" + getVersion() +
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
            ", testClientIdPrefix='" + getTestClientIdPrefix() + "'" +
            ", testOrderIdPrefix='" + getTestOrderIdPrefix() + "'" +
            ", liveClientIdPrefix='" + getLiveClientIdPrefix() + "'" +
            ", liveOrderIdPrefix='" + getLiveOrderIdPrefix() + "'" +
            ", testDailyAmountLimit=" + getTestDailyAmountLimit() +
            ", liveDailyAmountLimit=" + getLiveDailyAmountLimit() +
            "}";
    }
}
