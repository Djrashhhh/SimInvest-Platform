package com.example.MicroInvestApp.domain.market;

import com.example.MicroInvestApp.domain.enums.AlertType;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "Alert")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })

public class Alert implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "securityId", nullable = false)
    private SecurityStock securityStock;

    @NotNull(message = "Alert type cannot be null")
    @Column(name = "threshold", nullable = false)
    private BigDecimal threshold;

    @NotNull(message = "Alert name cannot be null")
    @Column(name = "alert_name", nullable = false, length = 100)
    private String alertName; // Optional field for naming the alert

    @NotNull(message = "Alert type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType; // Type of alert (e.g., price, volume, etc.)

    @NotNull(message = "Alert active cannot be null")
    @Column(name = "is_alert_active", nullable = false)
    private boolean isActive = true; // Indicates if the alert is active

    @NotNull(message = "Alert description cannot be null")
    @Column(name = "description", length = 500)
    private String description; // Optional field for alert description

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Timestamp for when the alert was created

    public Alert() {
        // Default constructor
    }

    public Alert(UserAccount userAccount, SecurityStock securityStock, BigDecimal threshold, String alertName, AlertType alertType, boolean isActive, String description) {
        this.userAccount = userAccount;
        this.securityStock = securityStock;
        this.threshold = threshold;
        this.alertName = alertName;
        this.alertType = alertType;
        this.isActive = isActive;
        this.description = description;
    }

    // Getters and Setters
    public Long getAlertId() {
        return alertId;
    }
    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }
    public UserAccount getUserAccount() {
        return userAccount;
    }
    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    public SecurityStock getSecurityStock() {
        return securityStock;
    }
    public void setSecurityStock(SecurityStock securityStock) {
        this.securityStock = securityStock;
    }
    public BigDecimal getThreshold() {
        return threshold;
    }
    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }
    public String getAlertName() {
        return alertName;
    }
    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }
    public AlertType getAlertType() {
        return alertType;
    }
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "Alert{" +
                "alertId=" + alertId +
                ", userAccount=" + userAccount +
                ", securityStock=" + securityStock +
                ", threshold=" + threshold +
                ", alertName='" + alertName + '\'' +
                ", alertType=" + alertType +
                ", isActive=" + isActive +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

}
