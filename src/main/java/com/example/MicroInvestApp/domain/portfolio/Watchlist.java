package com.example.MicroInvestApp.domain.portfolio;

import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "watchlist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "name"})
        },
        indexes = {
                @Index(name = "idx_watchlist_user_id", columnList = "user_id"),
                @Index(name = "idx_watchlist_created_at", columnList = "created_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Watchlist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watchlist_id")
    private Long watchlistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watchlist_user"))
    @NotNull(message = "User account cannot be null")
    private UserAccount userAccount;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "watchlist_securities",
            joinColumns = @JoinColumn(name = "watchlist_id", foreignKey = @ForeignKey(name = "fk_watchlist_securities_watchlist")),
            inverseJoinColumns = @JoinColumn(name = "security_id", foreignKey = @ForeignKey(name = "fk_watchlist_securities_security"))
    )
    private Set<SecurityStock> securities = new HashSet<>();

    @NotNull(message = "Name cannot be null")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Constructors
    public Watchlist() {
    }

    public Watchlist(UserAccount userAccount, String name, String description) {
        this.userAccount = userAccount;
        this.name = name;
        this.description = description;
        this.securities = new HashSet<>();
    }

    // Getters and Setters
    public Long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(Long watchlistId) {
        this.watchlistId = watchlistId;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Set<SecurityStock> getSecurities() {
        return securities;
    }

    public void setSecurities(Set<SecurityStock> securities) {
        this.securities = securities != null ? securities : new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }


    // Utility methods
    public void addSecurity(SecurityStock security) {
        if (security != null) {
            this.securities.add(security);
        }
    }

    public void removeSecurity(SecurityStock security) {
        if (security != null) {
            this.securities.remove(security);
        }
    }

    public boolean containsSecurity(SecurityStock security) {
        return security != null && this.securities.contains(security);
    }

    public int getSecurityCount() {
        return this.securities.size();
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Watchlist)) return false;
        Watchlist watchlist = (Watchlist) o;
        return Objects.equals(watchlistId, watchlist.watchlistId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(watchlistId);
    }

    @Override
    public String toString() {
        return "Watchlist{" +
                "watchlistId=" + watchlistId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", securityCount=" + (securities != null ? securities.size() : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}