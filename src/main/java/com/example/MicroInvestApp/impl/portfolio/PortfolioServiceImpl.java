package com.example.MicroInvestApp.impl.portfolio;

import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.user.UserAccount;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.CreatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.RequestDTOs.UpdatePortfolioRequestDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioResponseDTO;
import com.example.MicroInvestApp.dto.portfolio.ResponseDTOs.PortfolioSummaryResponseDTO;
import com.example.MicroInvestApp.exception.portfolio.PortfolioNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.InsufficientCashException;
import com.example.MicroInvestApp.exception.portfolio.PortfolioAlreadyExistsException;
import com.example.MicroInvestApp.repositories.portfolio.PortfolioRepository;
import com.example.MicroInvestApp.repositories.portfolio.PositionRepository;
import com.example.MicroInvestApp.repositories.user.UserAccountRepository;
import com.example.MicroInvestApp.service.portfolio.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of PortfolioService
 * Handles all portfolio-related business logic
 */
@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository,
                                PositionRepository positionRepository,
                                UserAccountRepository userAccountRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public PortfolioResponseDTO createPortfolio(Long userId, CreatePortfolioRequestDTO request) {
        logger.info("Creating portfolio for user ID: {}", userId);

        // Check if user already has an active portfolio
        if (hasActivePortfolio(userId)) {
            throw new PortfolioAlreadyExistsException("User already has an active portfolio");
        }

        // Get user account
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Create new portfolio
        Portfolio portfolio = new Portfolio(
                userAccount,
                request.getPortfolioName(),
                request.getInitialCashBalance(),
                request.getInitialCashBalance()
        );

        // Save portfolio
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Portfolio created successfully with ID: {}", savedPortfolio.getPortfolioId());

        return convertToResponse(savedPortfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PortfolioResponseDTO> getPortfolioById(Long portfolioId) {
        logger.debug("Fetching portfolio by ID: {}", portfolioId);

        return portfolioRepository.findById(portfolioId)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PortfolioResponseDTO> getPortfolioByUserId(Long userId) {
        logger.debug("Fetching portfolio by user ID: {}", userId);

        return portfolioRepository.findByUserIdWithUser(userId)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PortfolioResponseDTO> getActivePortfolioByUserId(Long userId) {
        logger.debug("Fetching active portfolio by user ID: {}", userId);

        return portfolioRepository.findActiveByUserIdWithUser(userId)
                .map(this::convertToResponse);
    }

    @Override
    public PortfolioResponseDTO updatePortfolio(Long portfolioId, UpdatePortfolioRequestDTO request) {
        logger.info("Updating portfolio ID: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Update fields if provided
        if (request.getPortfolioName() != null) {
            portfolio.setPortfolioName(request.getPortfolioName());
        }

        if (request.getIsActive() != null) {
            portfolio.setActive(request.getIsActive());
        }

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Portfolio updated successfully: {}", portfolioId);

        return convertToResponse(updatedPortfolio);
    }

    @Override
    public void deletePortfolio(Long portfolioId) {
        logger.info("Deleting portfolio ID: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Check if portfolio has positions
        Long positionCount = positionRepository.countByPortfolioId(portfolioId);
        if (positionCount > 0) {
            throw new IllegalStateException("Cannot delete portfolio with existing positions");
        }

        portfolioRepository.delete(portfolio);
        logger.info("Portfolio deleted successfully: {}", portfolioId);
    }

    @Override
    public PortfolioResponseDTO updatePortfolioStatus(Long portfolioId, boolean isActive) {
        logger.info("Updating portfolio status - ID: {}, Active: {}", portfolioId, isActive);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        portfolio.setActive(isActive);
        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);

        return convertToResponse(updatedPortfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> getAllActivePortfolios() {
        logger.debug("Fetching all active portfolios");

        return portfolioRepository.findAllActive()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> getPortfoliosByValueRange(BigDecimal minValue, BigDecimal maxValue) {
        logger.debug("Fetching portfolios by value range: {} - {}", minValue, maxValue);

        return portfolioRepository.findByTotalValueBetween(minValue, maxValue)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> getPortfoliosCreatedAfter(Instant date) {
        logger.debug("Fetching portfolios created after: {}", date);

        return portfolioRepository.findCreatedAfter(date)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryResponseDTO getPortfolioSummary(Long portfolioId) {
        logger.debug("Generating portfolio summary for ID: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Get aggregated data
        Long positionCount = positionRepository.countByPortfolioId(portfolioId);
        BigDecimal totalCurrentValue = positionRepository.getTotalCurrentValue(portfolioId);
        BigDecimal totalUnrealizedGainLoss = positionRepository.getTotalUnrealizedGainLoss(portfolioId);
        BigDecimal totalRealizedGainLoss = positionRepository.getTotalRealizedGainLoss(portfolioId);

        // Calculate invested amount (total value - cash balance)
        BigDecimal investedAmount = portfolio.getTotalValue().subtract(portfolio.getCashBalance());
        BigDecimal totalGainLoss = totalUnrealizedGainLoss.add(totalRealizedGainLoss);

        PortfolioSummaryResponseDTO summary = new PortfolioSummaryResponseDTO(
                portfolio.getPortfolioId(),
                portfolio.getPortfolioName(),
                portfolio.getTotalValue(),
                portfolio.getCashBalance(),
                investedAmount,
                totalGainLoss
        );

        summary.setPositionCount(positionCount);
        summary.setLastUpdated(portfolio.getLastUpdated());

        return summary;
    }

    @Override
    public PortfolioResponseDTO recalculatePortfolioValue(Long portfolioId) {
        logger.info("Recalculating portfolio value for ID: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Get total current value of all positions
        BigDecimal totalPositionValue = positionRepository.getTotalCurrentValue(portfolioId);

        // Update total value (positions + cash)
        BigDecimal newTotalValue = totalPositionValue.add(portfolio.getCashBalance());
        portfolio.setTotalValue(newTotalValue);

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Portfolio value recalculated - ID: {}, New Value: {}", portfolioId, newTotalValue);

        return convertToResponse(updatedPortfolio);
    }

    @Override
    public PortfolioResponseDTO addCash(Long portfolioId, BigDecimal amount) {
        logger.info("Adding cash to portfolio - ID: {}, Amount: {}", portfolioId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cash amount must be positive");
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Update cash balance and total value
        portfolio.setCashBalance(portfolio.getCashBalance().add(amount));
        portfolio.setTotalValue(portfolio.getTotalValue().add(amount));

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Cash added successfully - Portfolio ID: {}, New Cash Balance: {}",
                portfolioId, updatedPortfolio.getCashBalance());

        return convertToResponse(updatedPortfolio);
    }

    @Override
    public PortfolioResponseDTO withdrawCash(Long portfolioId, BigDecimal amount) {
        logger.info("Withdrawing cash from portfolio - ID: {}, Amount: {}", portfolioId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with ID: " + portfolioId));

        // Check if sufficient cash available
        if (portfolio.getCashBalance().compareTo(amount) < 0) {
            throw new InsufficientCashException("Insufficient cash balance for withdrawal");
        }

        // Update cash balance and total value
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(amount));
        portfolio.setTotalValue(portfolio.getTotalValue().subtract(amount));

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        logger.info("Cash withdrawn successfully - Portfolio ID: {}, New Cash Balance: {}",
                portfolioId, updatedPortfolio.getCashBalance());

        return convertToResponse(updatedPortfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActivePortfolio(Long userId) {
        return portfolioRepository.hasActivePortfolio(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalActivePortfolioCount() {
        return portfolioRepository.countActivePortfolios();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPortfolioValue() {
        BigDecimal total = portfolioRepository.getTotalPortfolioValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> findPortfoliosByName(String namePattern) {
        logger.debug("Finding portfolios by name pattern: {}", namePattern);

        return portfolioRepository.findByPortfolioNameContainingIgnoreCase(namePattern)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponseDTO> getPortfoliosWithCashBalance(BigDecimal minCashBalance) {
        logger.debug("Finding portfolios with cash balance >= {}", minCashBalance);

        return portfolioRepository.findWithCashBalanceGreaterThan(minCashBalance)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePortfolioOwnership(Long portfolioId, Long userId) {
        Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);
        return portfolio.isPresent() &&
                portfolio.get().getUserAccount().getUserId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Portfolio> getPortfolioEntityById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId);
    }

    //Method to synchronize user account balance with portfolio cash balance
    private void syncUserAccountBalance(Long userId) {
        // Get user's portfolio total value
        Optional<Portfolio> portfolio = portfolioRepository.findActiveByUserIdWithUser(userId);

        if (portfolio.isPresent()) {
            UserAccount userAccount = portfolio.get().getUserAccount();

            // Update user account balance to match portfolio
            userAccount.setCurrentVirtualBalance(portfolio.get().getCashBalance().doubleValue());
            userAccount.setTotalInvestedAmount(
                    portfolio.get().getTotalValue().subtract(portfolio.get().getCashBalance()).doubleValue()
            );

            // Calculate total returns from positions
            BigDecimal totalReturns = positionRepository.getTotalRealizedGainLoss(portfolio.get().getPortfolioId())
                    .add(positionRepository.getTotalUnrealizedGainLoss(portfolio.get().getPortfolioId()));
            userAccount.setTotalReturns(totalReturns.doubleValue());

            userAccountRepository.save(userAccount);
        }
    }

    // Private helper methods
    private PortfolioResponseDTO convertToResponse(Portfolio portfolio) {
        // Get additional calculated data
        Long positionCount = positionRepository.countByPortfolioId(portfolio.getPortfolioId());
        BigDecimal totalUnrealizedGainLoss = positionRepository.getTotalUnrealizedGainLoss(portfolio.getPortfolioId());
        BigDecimal totalRealizedGainLoss = positionRepository.getTotalRealizedGainLoss(portfolio.getPortfolioId());

        PortfolioResponseDTO response = new PortfolioResponseDTO(
                portfolio.getPortfolioId(),
                portfolio.getUserAccount().getUserId(),
                portfolio.getPortfolioName(),
                portfolio.getTotalValue(),
                portfolio.getCashBalance(),
                portfolio.getCreatedDate(),
                portfolio.getLastUpdated(),
                portfolio.isActive()
        );

        response.setPositionCount(positionCount);
        response.setTotalUnrealizedGainLoss(totalUnrealizedGainLoss != null ? totalUnrealizedGainLoss : BigDecimal.ZERO);
        response.setTotalRealizedGainLoss(totalRealizedGainLoss != null ? totalRealizedGainLoss : BigDecimal.ZERO);

        return response;
    }
}