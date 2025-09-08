package com.example.MicroInvestApp.domain.portfolio;

import com.example.MicroInvestApp.domain.enums.Currency;
import com.example.MicroInvestApp.domain.enums.DividendFrequency;
import com.example.MicroInvestApp.domain.enums.DividendType;
import com.example.MicroInvestApp.domain.market.SecurityStock;
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
import java.time.LocalDate;


@SuppressWarnings({ "serial", "deprecation",  })
@Entity
@Table(name = "Dividend")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })


public class Dividend  implements Serializable {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long dividendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "securityId", nullable = false)
    private SecurityStock securityStock; // The stock for which the dividend is paid

    @NotNull(message = "Dividend date cannot be null")
    @Column(name = "dividend_amount", nullable = false)
    private BigDecimal amountPerShare; // The amount of the dividend paid per share

    @CreationTimestamp
    @Column(name = "payment_date", nullable = false, updatable = false)
    private Instant paymentDate; // The date when the dividend is paid

    @NotNull(message = "Ex-dividend date cannot be null")
    @Column(name = "ex_dividend_date", nullable = false)
    private LocalDate exDividendDate; // The date when the dividend record was created

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency; // The currency in which the dividend is paid

    @Enumerated(EnumType.STRING)
    @Column(name = "dividend_type", nullable = false)
    private DividendType dividendType; // Type of dividend (e.g., cash, stock, etc.)

    @Enumerated(EnumType.STRING)
    @Column(name = "dividend_frequency", nullable = false)
    private DividendFrequency dividendFrequency; // Frequency of the dividend payment (e.g., quarterly, annually)


    public Dividend() {
        // Default constructor
    }

    public Dividend(SecurityStock securityStock, BigDecimal amountPerShare, Instant paymentDate, LocalDate exDividendDate, Currency currency, DividendType dividendType, DividendFrequency dividendFrequency) {
        this.securityStock = securityStock;
        this.amountPerShare = amountPerShare;
        this.paymentDate = paymentDate;
        this.exDividendDate = exDividendDate;
        this.currency = currency;
        this.dividendType = dividendType;
        this.dividendFrequency = dividendFrequency;
    }

    public Long getDividendId() {
        return dividendId;
    }
    public void setDividendId(Long dividendId) {
        this.dividendId = dividendId;
    }
    public SecurityStock getSecurityStock() {
        return securityStock;
    }
    public void setSecurityStock(SecurityStock securityStock) {
        this.securityStock = securityStock;
    }
    public BigDecimal getAmountPerShare() {
        return amountPerShare;
    }
    public void setAmountPerShare(BigDecimal amountPerShare) {
        this.amountPerShare = amountPerShare;
    }
    public Instant getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }
    public LocalDate getExDividendDate() {
        return exDividendDate;
    }
    public void setExDividendDate(LocalDate exDividendDate) {
        this.exDividendDate = exDividendDate;
    }
    public Currency getCurrency() {
        return currency;
    }
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public DividendType getDividendType() {
        return dividendType;
    }
    public void setDividendType(DividendType dividendType) {
        this.dividendType = dividendType;
    }
    public DividendFrequency getDividendFrequency() {
        return dividendFrequency;
    }
    public void setDividendFrequency(DividendFrequency dividendFrequency) {
        this.dividendFrequency = dividendFrequency;
    }


    @Override
    public String toString() {
        return "Dividend{" +
                "dividendId=" + dividendId +
                ", securityStock=" + securityStock +
                ", amountPerShare=" + amountPerShare +
                ", paymentDate=" + paymentDate +
                ", exDividendDate=" + exDividendDate +
                ", currency=" + currency +
                ", dividendType=" + dividendType +
                ", dividendFrequency=" + dividendFrequency +
                '}';
    }

}
