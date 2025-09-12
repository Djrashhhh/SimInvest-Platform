package com.example.MicroInvestApp.controller.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Dividend;
import com.example.MicroInvestApp.domain.enums.DividendFrequency;
import com.example.MicroInvestApp.domain.enums.DividendType;
import com.example.MicroInvestApp.exception.portfolio.DividendNotFoundException;
import com.example.MicroInvestApp.service.portfolio.DividendService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dividends")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class DividendController {

    private final DividendService dividendService;

    @Autowired
    public DividendController(DividendService dividendService) {
        this.dividendService = dividendService;
    }

    /**
     * Create a new dividend record
     */
    @PostMapping
    public ResponseEntity<Dividend> createDividend(@Valid @RequestBody Dividend dividend) {
        Dividend createdDividend = dividendService.createDividend(dividend);
        return new ResponseEntity<>(createdDividend, HttpStatus.CREATED);
    }

    /**
     * Get dividend by ID
     */
    @GetMapping("/{dividendId}")
    public ResponseEntity<Dividend> getDividendById(@PathVariable Long dividendId) {
        return dividendService.getDividendById(dividendId)
                .map(dividend -> ResponseEntity.ok(dividend))
                .orElseThrow(() -> new DividendNotFoundException("Dividend not found with ID: " + dividendId));
    }

    /**
     * Get all dividends with pagination
     */
    @GetMapping
    public ResponseEntity<Page<Dividend>> getAllDividends(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "exDividendDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Dividend> dividends = dividendService.getAllDividends(pageable);

        return ResponseEntity.ok(dividends);
    }

    /**
     * Get all dividends for a specific security
     */
    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<Dividend>> getDividendsBySecurityId(@PathVariable Long securityId) {
        List<Dividend> dividends = dividendService.getDividendsBySecurityId(securityId);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividends for a security with pagination
     */
    @GetMapping("/security/{securityId}/paginated")
    public ResponseEntity<Page<Dividend>> getDividendsBySecurityIdPaginated(
            @PathVariable Long securityId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "exDividendDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Dividend> dividends = dividendService.getDividendsBySecurityId(securityId, pageable);

        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividends for a security within date range
     */
    @GetMapping("/security/{securityId}/date-range")
    public ResponseEntity<List<Dividend>> getDividendsBySecurityAndDateRange(
            @PathVariable Long securityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Dividend> dividends = dividendService.getDividendsBySecurityAndDateRange(securityId, startDate, endDate);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividends by frequency
     */
    @GetMapping("/frequency/{frequency}")
    public ResponseEntity<List<Dividend>> getDividendsByFrequency(@PathVariable DividendFrequency frequency) {
        List<Dividend> dividends = dividendService.getDividendsByFrequency(frequency);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get dividends by type
     */
    @GetMapping("/type/{dividendType}")
    public ResponseEntity<List<Dividend>> getDividendsByType(@PathVariable DividendType dividendType) {
        List<Dividend> dividends = dividendService.getDividendsByType(dividendType);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get upcoming dividends
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Dividend>> getUpcomingDividends() {
        List<Dividend> dividends = dividendService.getUpcomingDividends();
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get recent dividends (within specified days)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Dividend>> getRecentDividends(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        List<Dividend> dividends = dividendService.getRecentDividends(days);
        return ResponseEntity.ok(dividends);
    }

    /**
     * Get total dividend amount for a security
     */
    @GetMapping("/security/{securityId}/total-amount")
    public ResponseEntity<BigDecimal> getTotalDividendAmount(@PathVariable Long securityId) {
        BigDecimal totalAmount = dividendService.getTotalDividendAmountBySecurityId(securityId);
        return ResponseEntity.ok(totalAmount);
    }

    /**
     * Get total dividend amount for a security within date range
     */
    @GetMapping("/security/{securityId}/total-amount/date-range")
    public ResponseEntity<BigDecimal> getTotalDividendAmountByDateRange(
            @PathVariable Long securityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal totalAmount = dividendService.getTotalDividendAmountBySecurityAndDateRange(securityId, startDate, endDate);
        return ResponseEntity.ok(totalAmount);
    }

    /**
     * Get dividend yield for a security
     */
    @GetMapping("/security/{securityId}/yield")
    public ResponseEntity<BigDecimal> getDividendYield(@PathVariable Long securityId) {
        BigDecimal yield = dividendService.getDividendYield(securityId);
        return ResponseEntity.ok(yield);
    }

    /**
     * Update dividend information
     */
    @PutMapping("/{dividendId}")
    public ResponseEntity<Dividend> updateDividend(
            @PathVariable Long dividendId,
            @Valid @RequestBody Dividend updatedDividend) {
        Dividend dividend = dividendService.updateDividend(dividendId, updatedDividend);
        return ResponseEntity.ok(dividend);
    }

    /**
     * Delete dividend by ID
     */
    @DeleteMapping("/{dividendId}")
    public ResponseEntity<Void> deleteDividend(@PathVariable Long dividendId) {
        dividendService.deleteDividend(dividendId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if dividend exists
     */
    @GetMapping("/{dividendId}/exists")
    public ResponseEntity<Boolean> dividendExists(@PathVariable Long dividendId) {
        boolean exists = dividendService.dividendExists(dividendId);
        return ResponseEntity.ok(exists);
    }
}