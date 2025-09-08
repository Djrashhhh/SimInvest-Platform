// ===== FIXED Portfolio.java =====
package com.example.MicroInvestApp.domain.portfolio;

import com.example.MicroInvestApp.domain.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@SuppressWarnings({ "serial", "deprecation" })
@Entity
@Table(name = "Portfolios", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "user_id"),
        @Index(name = "idx_portfolio_active", columnList = "portfolio_active")
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Portfolio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portfolioId;

    // ✅ FIXED: Column name mapping
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Fixed: was "userId"
    @JsonIgnore // ✅ FIXED: Prevent circular reference
    private UserAccount userAccount;

    @NotBlank(message = "Portfolio name cannot be blank")
    @Size(min = 1, max = 100, message = "Portfolio name must be between 1 and 100 characters")
    @Column(name = "portfolio_name", nullable = false, length = 100)
    private String portfolioName;

    @PositiveOrZero(message = "Total value cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid total value format")
    @Column(name = "total_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @PositiveOrZero(message = "Cash balance cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Invalid cash balance format")
    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "portfolio_active", nullable = false)
    private boolean isActive = true; // ✅ FIXED: Default value

    // ✅ FIXED: Relationship mapping and JSON handling
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore // ✅ FIXED: Prevent circular reference and serialization issues
    private List<Position> positions;

    // Default constructor
    public Portfolio() {
        this.isActive = true;
    }

    public Portfolio(UserAccount userAccount, String portfolioName, BigDecimal totalValue, BigDecimal cashBalance) {
        this();
        this.userAccount = userAccount;
        this.portfolioName = portfolioName;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
    }

    // ✅ FIXED: Added business logic methods
    public BigDecimal getInvestedAmount() {
        return totalValue.subtract(cashBalance);
    }

    public boolean hasSufficientCash(BigDecimal amount) {
        return cashBalance.compareTo(amount) >= 0;
    }

    public void addCash(BigDecimal amount) {
        this.cashBalance = this.cashBalance.add(amount);
        this.totalValue = this.totalValue.add(amount);
    }

    public void subtractCash(BigDecimal amount) {
        if (!hasSufficientCash(amount)) {
            throw new IllegalArgumentException("Insufficient cash balance");
        }
        this.cashBalance = this.cashBalance.subtract(amount);
        this.totalValue = this.totalValue.subtract(amount);
    }

    // Getters and Setters
    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public UserAccount getUserAccount() { return userAccount; }
    public void setUserAccount(UserAccount userAccount) { this.userAccount = userAccount; }

    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<Position> getPositions() { return positions; }
    public void setPositions(List<Position> positions) { this.positions = positions; }

    @Override
    public String toString() {
        return "Portfolio{" +
                "portfolioId=" + portfolioId +
                ", portfolioName='" + portfolioName + '\'' +
                ", totalValue=" + totalValue +
                ", cashBalance=" + cashBalance +
                ", isActive=" + isActive +
                '}';
    }

    @PrePersist
    @PreUpdate
    private void validatePortfolioState() {
        if (totalValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Total portfolio value cannot be negative");
        }
        if (cashBalance.compareTo(totalValue) > 0) {
            throw new IllegalStateException("Cash balance cannot exceed total value");
        }
    }
}