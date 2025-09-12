package com.example.MicroInvestApp.domain.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.example.MicroInvestApp.domain.enums.AccountStatus;
import com.example.MicroInvestApp.domain.enums.Currency;
import com.example.MicroInvestApp.domain.enums.RiskTolerance;
import com.example.MicroInvestApp.domain.enums.SecurityQuestion;
import com.example.MicroInvestApp.domain.market.Alert;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.portfolio.Watchlist;
import com.example.MicroInvestApp.exception.InsufficientFundsException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.bcrypt.BCrypt;

@SuppressWarnings({ "serial", "deprecation" })
@Entity
@Table(name = "user_account")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class UserAccount implements Serializable {

    // Unique user ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name="email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Column(name="username",nullable = false, unique = true)
    private String username;

    // FIXED: Use only passwordHash field for storing encrypted passwords
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String passwordHash;

    @NotNull(message = "Risk tolerance cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_tolerance", nullable = false)
    private RiskTolerance riskTolerance;

    @NotNull(message = "Account status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // Security Question fields
    @NotNull(message = "Security question is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "security_question")
    private SecurityQuestion securityQuestion;

    @NotBlank(message = "Security answer is required")
    @Column(name = "security_answer", nullable = false, length = 100)
    @JsonIgnore
    private String hashedAnswer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Financial/virtual account fields
    @Column(name = "initial_virtual_balance", nullable = false)
    private double initialVirtualBalance = 10000.0;

    @Min(value = 0, message = "Balance cannot be negative")
    @Column(name = "current_virtual_balance", nullable = false)
    private double currentVirtualBalance = 10000.0;

    @Min(value = 0, message = "Invested amount cannot be negative")
    @Column(name = "total_invested_amount")
    private double totalInvestedAmount = 0.0;

    @NotNull(message = "Total returns cannot be null")
    @Column(name = "total_returns")
    private double totalReturns = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_currency", nullable = false)
    private Currency accountCurrency = Currency.USD;

    // RELATIONSHIPS WITH CASCADE AND FETCH STRATEGIES
    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Achievement> achievements;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AuditLog> auditLogs;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<UserSession> userSessions;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Portfolio> portfolios;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Watchlist> watchlists;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Alert> alerts;

    // No argument constructor
    public UserAccount() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.emailVerified = false;
    }

    // Overloaded constructor for the virtual account fields
    public UserAccount(double initialVirtualBalance, double currentVirtualBalance, double totalInvestedAmount,
                       double totalReturns, Currency accountCurrency) {
        this();
        this.initialVirtualBalance = initialVirtualBalance;
        this.currentVirtualBalance = initialVirtualBalance;
        this.totalInvestedAmount = totalInvestedAmount;
        this.totalReturns = totalReturns;
        this.accountCurrency = accountCurrency;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (currentVirtualBalance == 0.0) {
            currentVirtualBalance = initialVirtualBalance;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public boolean canInvest(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Investment amount must be positive");
        }
        return currentVirtualBalance >= amount && accountStatus == AccountStatus.ACTIVE;
    }

    public void updateBalanceAfterInvestment(double investmentAmount) {
        if (!canInvest(investmentAmount)) {
            throw new InsufficientFundsException("Cannot invest: insufficient funds or inactive account");
        }
        this.currentVirtualBalance -= investmentAmount;
        this.totalInvestedAmount += investmentAmount;
    }

    public void updateBalanceAfterSale(double saleAmount, double gains) {
        if (saleAmount <= 0) {
            throw new IllegalArgumentException("Sale amount must be positive");
        }
        this.currentVirtualBalance += saleAmount;
        this.totalInvestedAmount -= (saleAmount - gains);
        this.totalReturns += gains;
    }

    public double getTotalPortfolioValue() {
        return currentVirtualBalance + totalInvestedAmount;
    }

    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getNetWorth() {
        return currentVirtualBalance + totalInvestedAmount + totalReturns;
    }

    public double getReturnOnInvestment() {
        return totalInvestedAmount > 0 ? (totalReturns / totalInvestedAmount) * 100 : 0.0;
    }

    // FIXED: Password handling methods
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setPassword(String rawPassword) {
        // This method should be used during registration to set the password
        this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    // For Spring Security - returns the hashed password
    public String getPassword() {
        return this.passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // FIXED: Security answer handling
    public void setSecurityAnswer(String securityAnswer) {
        if (securityAnswer != null && !securityAnswer.trim().isEmpty()) {
            this.hashedAnswer = hashSecurityAnswer(securityAnswer);
        }
    }

    private String hashSecurityAnswer(String answer) {
        try {
            return BCrypt.hashpw(answer.toLowerCase().trim(), BCrypt.gensalt());
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash security answer", e);
        }
    }

    public boolean verifySecurityAnswer(String providedAnswer) {
        if (providedAnswer == null || this.hashedAnswer == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(providedAnswer.toLowerCase().trim(), this.hashedAnswer);
        } catch (Exception e) {
            return false;
        }
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public SecurityQuestion getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(SecurityQuestion securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getHashedAnswer() {
        return hashedAnswer;
    }

    public void setHashedAnswer(String hashedAnswer) {
        this.hashedAnswer = hashedAnswer;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Currency getAccountCurrency() {
        return accountCurrency;
    }

    public void setAccountCurrency(Currency accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public double getInitialVirtualBalance() {
        return initialVirtualBalance;
    }

    public void setInitialVirtualBalance(double initialVirtualBalance) {
        this.initialVirtualBalance = initialVirtualBalance;
    }

    public double getCurrentVirtualBalance() {
        return currentVirtualBalance;
    }

    public void setCurrentVirtualBalance(double currentVirtualBalance) {
        this.currentVirtualBalance = currentVirtualBalance;
    }

    public double getTotalInvestedAmount() {
        return totalInvestedAmount;
    }

    public void setTotalInvestedAmount(double totalInvestedAmount) {
        this.totalInvestedAmount = totalInvestedAmount;
    }

    public double getTotalReturns() {
        return totalReturns;
    }

    public void setTotalReturns(double totalReturns) {
        this.totalReturns = totalReturns;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public List<UserSession> getUserSessions() {
        return userSessions;
    }

    public void setUserSessions(List<UserSession> userSessions) {
        this.userSessions = userSessions;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    public List<Watchlist> getWatchlists() {
        return watchlists;
    }

    public void setWatchlists(List<Watchlist> watchlists) {
        this.watchlists = watchlists;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    // Spring Security methods
    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public void setActive(boolean active) {
        this.accountStatus = active ? AccountStatus.ACTIVE : AccountStatus.INACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", accountStatus=" + accountStatus +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}