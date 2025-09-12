package com.example.MicroInvestApp.impl.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Dividend;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.enums.DividendFrequency;
import com.example.MicroInvestApp.domain.enums.DividendType;
import com.example.MicroInvestApp.exception.SecurityNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.DividendNotFoundException;
import com.example.MicroInvestApp.repositories.portfolio.DividendRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.portfolio.DividendService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DividendServiceImpl implements DividendService {

    private final DividendRepository dividendRepository;
    private final SecurityStockRepository securityStockRepository;

    @Autowired
    public DividendServiceImpl(DividendRepository dividendRepository,
                               SecurityStockRepository securityStockRepository) {
        this.dividendRepository = dividendRepository;
        this.securityStockRepository = securityStockRepository;
    }

    @Override
    public Dividend createDividend(Dividend dividend) {
        validateDividend(dividend);

        // Verify security exists
        SecurityStock security = securityStockRepository.findById(dividend.getSecurityStock().getSecurityId())
                .orElseThrow(() -> new SecurityNotFoundException("Security not found"));

        dividend.setSecurityStock(security);
        return dividendRepository.save(dividend);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dividend> getDividendById(Long dividendId) {
        return dividendRepository.findById(dividendId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getDividendsBySecurityId(Long securityId) {
        verifySecurityExists(securityId);
        return dividendRepository.findBySecurityStockSecurityIdOrderByExDividendDateDesc(securityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Dividend> getDividendsBySecurityId(Long securityId, Pageable pageable) {
        verifySecurityExists(securityId);
        return dividendRepository.findBySecurityStockSecurityId(securityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getDividendsBySecurityAndDateRange(Long securityId, LocalDate startDate, LocalDate endDate) {
        verifySecurityExists(securityId);
        validateDateRange(startDate, endDate);
        return dividendRepository.findBySecurityStockSecurityIdAndExDividendDateBetween(
                securityId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getDividendsByFrequency(DividendFrequency frequency) {
        return dividendRepository.findByDividendFrequency(frequency);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getDividendsByType(DividendType dividendType) {
        return dividendRepository.findByDividendType(dividendType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getUpcomingDividends() {
        LocalDate today = LocalDate.now();
        return dividendRepository.findByExDividendDateAfterOrderByExDividendDateAsc(today);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dividend> getRecentDividends(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return dividendRepository.findRecentDividends(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDividendAmountBySecurityId(Long securityId) {
        verifySecurityExists(securityId);
        BigDecimal total = dividendRepository.sumDividendAmountBySecurityId(securityId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDividendAmountBySecurityAndDateRange(Long securityId, LocalDate startDate, LocalDate endDate) {
        verifySecurityExists(securityId);
        validateDateRange(startDate, endDate);
        BigDecimal total = dividendRepository.sumDividendAmountBySecurityIdAndDateRange(securityId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Dividend updateDividend(Long dividendId, Dividend updatedDividend) {
        Dividend existingDividend = dividendRepository.findById(dividendId)
                .orElseThrow(() -> new DividendNotFoundException("Dividend not found with ID: " + dividendId));

        validateDividend(updatedDividend);

        // Update fields
        existingDividend.setAmountPerShare(updatedDividend.getAmountPerShare());
        existingDividend.setExDividendDate(updatedDividend.getExDividendDate());
        existingDividend.setCurrency(updatedDividend.getCurrency());
        existingDividend.setDividendType(updatedDividend.getDividendType());
        existingDividend.setDividendFrequency(updatedDividend.getDividendFrequency());

        return dividendRepository.save(existingDividend);
    }

    @Override
    public void deleteDividend(Long dividendId) {
        if (!dividendRepository.existsById(dividendId)) {
            throw new DividendNotFoundException("Dividend not found with ID: " + dividendId);
        }
        dividendRepository.deleteById(dividendId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean dividendExists(Long dividendId) {
        return dividendRepository.existsById(dividendId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDividendYield(Long securityId) {
        SecurityStock security = securityStockRepository.findById(securityId)
                .orElseThrow(() -> new SecurityNotFoundException("Security not found"));

        BigDecimal currentPrice = security.getCurrentPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Get annual dividend amount (sum of dividends in the last 12 months)
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        LocalDate today = LocalDate.now();
        BigDecimal annualDividends = getTotalDividendAmountBySecurityAndDateRange(securityId, oneYearAgo, today);

        if (annualDividends.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Calculate yield: (Annual Dividends / Current Price) * 100
        return annualDividends.divide(currentPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Dividend> getAllDividends(Pageable pageable) {
        return dividendRepository.findAll(pageable);
    }

    // Private helper methods
    private void validateDividend(Dividend dividend) {
        if (dividend == null) {
            throw new IllegalArgumentException("Dividend cannot be null");
        }
        if (dividend.getAmountPerShare() == null || dividend.getAmountPerShare().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Dividend amount per share must be positive");
        }
        if (dividend.getExDividendDate() == null) {
            throw new IllegalArgumentException("Ex-dividend date cannot be null");
        }
        if (dividend.getSecurityStock() == null || dividend.getSecurityStock().getSecurityId() == null) {
            throw new IllegalArgumentException("Security stock cannot be null");
        }
    }

    private void verifySecurityExists(Long securityId) {
        if (!securityStockRepository.existsById(securityId)) {
            throw new SecurityNotFoundException("Security not found with ID: " + securityId);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}