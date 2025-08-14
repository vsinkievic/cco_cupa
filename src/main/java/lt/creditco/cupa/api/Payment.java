package lt.creditco.cupa.api;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {

    private String orderId;
    private String clientId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String statusDescription;
    private Instant createdAt = Instant.now();

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
