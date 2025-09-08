package com.example.MicroInvestApp.service.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioSummaryResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PortfolioService {

    PortfolioResponseDTO createPortfolio(Long userId, CreatePortfolioRequestDTO request);

    Optional<PortfolioResponseDTO> getPortfolioById(Long portfolioId);

    Optional<PortfolioResponseDTO> getPortfolioByUserId(Long userId);

    Optional<PortfolioResponseDTO> getActivePortfolioByUserId(Long userId);

    PortfolioResponseDTO updatePortfolio(Long portfolioId, UpdatePortfolioRequestDTO request);

    void deletePortfolio(Long portfolioId);

    PortfolioResponseDTO updatePortfolioStatus(Long portfolioId, boolean isActive);

    List<PortfolioResponseDTO> getAllActivePortfolios();

    List<PortfolioResponseDTO> getPortfoliosByValueRange(BigDecimal minValue, BigDecimal maxValue);

    List<PortfolioResponseDTO> getPortfoliosCreatedAfter(Instant date);

    PortfolioSummaryResponseDTO getPortfolioSummary(Long portfolioId);

    PortfolioResponseDTO recalculatePortfolioValue(Long portfolioId);

    PortfolioResponseDTO addCash(Long portfolioId, BigDecimal amount);

    PortfolioResponseDTO withdrawCash(Long portfolioId, BigDecimal amount);

    boolean hasActivePortfolio(Long userId);

    Long getTotalActivePortfolioCount();

    BigDecimal getTotalPortfolioValue();

    List<PortfolioResponseDTO> findPortfoliosByName(String namePattern);

    List<PortfolioResponseDTO> getPortfoliosWithCashBalance(BigDecimal minCashBalance);

    boolean validatePortfolioOwnership(Long portfolioId, Long userId);

    Optional<Portfolio> getPortfolioEntityById(Long portfolioId);
}
