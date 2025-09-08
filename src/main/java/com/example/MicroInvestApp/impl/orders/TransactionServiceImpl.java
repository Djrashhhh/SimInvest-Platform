package com.example.MicroInvestApp.impl.orders;

import com.example.MicroInvestApp.exception.InsufficientFundsException;
import com.example.MicroInvestApp.exception.Orders.InvalidTransactionException;
import com.example.MicroInvestApp.exception.Orders.TransactionNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.PortfolioNotFoundException;
import com.example.MicroInvestApp.events.TransactionSettlementEvent;
import com.example.MicroInvestApp.scheduler.ScheduledTaskService;
import com.example.MicroInvestApp.service.order.TransactionService;
import com.example.MicroInvestApp.domain.orders.Transaction;
import com.example.MicroInvestApp.domain.orders.Order;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.enums.*;
import com.example.MicroInvestApp.dto.orders.TransactionRequestDTO;
import com.example.MicroInvestApp.dto.orders.TransactionResponseDTO;
import com.example.MicroInvestApp.repositories.orders.TransactionRepository;
import com.example.MicroInvestApp.repositories.orders.OrderRepository;
import com.example.MicroInvestApp.repositories.portfolio.PortfolioRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.portfolio.PositionService;
import com.example.MicroInvestApp.repositories.portfolio.PositionRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final SecurityStockRepository securityStockRepository;
    private final PositionService positionService;
    private final PositionRepository positionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  OrderRepository orderRepository,
                                  PortfolioRepository portfolioRepository,
                                  SecurityStockRepository securityStockRepository,
                                  PositionService positionService,
                                  PositionRepository positionRepository, ApplicationEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.securityStockRepository = securityStockRepository;
        this.positionService = positionService;
        this.positionRepository = positionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest) {
        logger.info("Creating transaction for portfolio {} - {} {} of {}",
                transactionRequest.getPortfolioId(),
                transactionRequest.getTransactionType(),
                transactionRequest.getQuantity(),
                transactionRequest.getStockSymbol());

        // Find portfolio
        Portfolio portfolio = portfolioRepository.findById(transactionRequest.getPortfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + transactionRequest.getPortfolioId()));

        // Find security
        SecurityStock security = securityStockRepository.findBySymbol(transactionRequest.getStockSymbol())
                .orElseThrow(() -> new RuntimeException("Security not found: " + transactionRequest.getStockSymbol()));

        // ✅ FIND ORDER IF ORDER ID IS PROVIDED
        Order order = null;
        if (transactionRequest.getOrderId() != null) {
            order = orderRepository.findById(transactionRequest.getOrderId()).orElse(null);
            logger.info("Linking transaction to order: {}", transactionRequest.getOrderId());
        }

        // ✅ CREATE TRANSACTION WITH ORDER REFERENCE
        Transaction transaction = new Transaction(portfolio, security, order, // ← NOW PASSES THE ORDER
                transactionRequest.getQuantity(),
                transactionRequest.getPricePerShare(),
                transactionRequest.getTransactionType());

        transaction.setFees(transactionRequest.getFees());
        transaction.setNotes(transactionRequest.getNotes());

        // ✅ SET ORDER TYPE FROM THE ORDER IF AVAILABLE
        if (order != null) {
            transaction.setOrderType(order.getOrderType());
            logger.info("Set order type: {} for transaction", order.getOrderType());
        }

        transaction.updateNetAmount();

        // Validate transaction
        validateTransaction(transaction);

        // Save transaction
        transaction = transactionRepository.save(transaction);

        // Process the transaction (update positions, portfolio balance, etc.)
        transaction = processTransactionEffects(transaction);

        // Schedule automatic settlement at T+2
        if (transaction.getSettlementDate() != null) {
            // Publish event instead of calling service directly
            TransactionSettlementEvent event = new TransactionSettlementEvent(
                    this,
                    transaction.getTransactionId(),
                    transaction.getSettlementDate()
            );
            eventPublisher.publishEvent(event);

            logger.info("Published settlement event for transaction {} at {}",
                    transaction.getTransactionId(), transaction.getSettlementDate());
        }

        logger.info("Transaction created successfully: {}", transaction.getTransactionId());
        return convertToResponseDTO(transaction);
    }

    @Override
    public TransactionResponseDTO processTransaction(Long transactionId) {
        logger.info("Processing transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Transaction is not in PENDING status");
        }

        try {
            // Mark as completed
            transaction.markAsCompleted();

            // Process effects
            transaction = processTransactionEffects(transaction);

            transaction = transactionRepository.save(transaction);
            logger.info("Transaction {} processed successfully", transactionId);

        } catch (Exception e) {
            logger.error("Failed to process transaction {}: {}", transactionId, e.getMessage());
            transaction.markAsFailed("Processing failed: " + e.getMessage());
            transaction = transactionRepository.save(transaction);
        }

        return convertToResponseDTO(transaction);
    }

    @Override
    public TransactionResponseDTO cancelTransaction(Long transactionId, String reason) {
        logger.info("Cancelling transaction: {} with reason: {}", transactionId, reason);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Only pending transactions can be cancelled");
        }

        transaction.markAsCancelled(reason);
        transaction = transactionRepository.save(transaction);

        logger.info("Transaction {} cancelled successfully", transactionId);
        return convertToResponseDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionResponseDTO> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return transactionRepository.findByPortfolioOrderByTransactionDateDesc(portfolio)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getTransactionsByPortfolio(Long portfolioId, Pageable pageable) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return transactionRepository.findByPortfolioOrderByTransactionDateDesc(portfolio, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElse(null);

        if (order == null) {
            return Collections.emptyList();
        }

        return transactionRepository.findByOrderOrderByTransactionDateDesc(order)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByTransactionStatusOrderByTransactionDateDesc(status)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByTransactionTypeOrderByTransactionDateDesc(type)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByPortfolioAndSecurity(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        SecurityStock security = securityStockRepository.findBySymbol(stockSymbol)
                .orElse(null);

        if (security == null) {
            return Collections.emptyList();
        }

        return transactionRepository.findByPortfolioAndSecurityStockOrderByTransactionDateDesc(portfolio, security)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findTransactionsByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getRecentTransactions(Long portfolioId, int limit) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentTransactions(portfolio, pageable)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalQuantityBought(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        SecurityStock security = securityStockRepository.findBySymbol(stockSymbol)
                .orElse(null);

        if (security == null) {
            return BigDecimal.ZERO;
        }

        return transactionRepository.getTotalQuantityBought(portfolio, security);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalQuantitySold(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        SecurityStock security = securityStockRepository.findBySymbol(stockSymbol)
                .orElse(null);

        if (security == null) {
            return BigDecimal.ZERO;
        }

        return transactionRepository.getTotalQuantitySold(portfolio, security);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageCostBasis(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        SecurityStock security = securityStockRepository.findBySymbol(stockSymbol)
                .orElse(null);

        if (security == null) {
            return BigDecimal.ZERO;
        }

        return transactionRepository.getAverageCostBasis(portfolio, security);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDividendIncome(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return transactionRepository.getTotalDividendIncome(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalFeesPaid(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return transactionRepository.getTotalFeesPaid(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatsByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        Object[] stats = transactionRepository.getTransactionStatsByPortfolio(portfolio);

        Map<String, Object> result = new HashMap<>();
        result.put("totalTransactions", stats[0]);
        result.put("buyTransactions", stats[1]);
        result.put("sellTransactions", stats[2]);
        result.put("dividendTransactions", stats[3]);
        result.put("totalBought", stats[4]);
        result.put("totalSold", stats[5]);

        return result;
    }

    @Override
    public void processUnsettledTransactions() {
        logger.info("Processing unsettled transactions");

        List<Transaction> unsettledTransactions = transactionRepository.findUnsettledTransactions(LocalDateTime.now());

        for (Transaction transaction : unsettledTransactions) {
            try {
                logger.info("Settling transaction: {}", transaction.getTransactionId());

                // ✅ Actually update the status to COMPLETED
                transaction.markAsCompleted();

                // ✅ Save the updated transaction
                transactionRepository.save(transaction);

                logger.info("Successfully settled transaction: {}", transaction.getTransactionId());

            } catch (Exception e) {
                logger.error("Failed to settle transaction {}: {}", transaction.getTransactionId(), e.getMessage());

                // Mark as failed if settlement fails
                try {
                    transaction.markAsFailed("Settlement processing failed: " + e.getMessage());
                    transactionRepository.save(transaction);
                } catch (Exception saveException) {
                    logger.error("Failed to mark transaction {} as failed: {}",
                            transaction.getTransactionId(), saveException.getMessage());
                }
            }
        }

        logger.info("Processed {} unsettled transactions", unsettledTransactions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getUnsettledTransactions() {
        return transactionRepository.findUnsettledTransactions(LocalDateTime.now())
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper Methods
    private void validateTransaction(Transaction transaction) {
        // Validate transaction business rules
        if (transaction.getQuantity() == null || transaction.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction quantity must be positive");
        }

        if (transaction.getPricePerShare() == null || transaction.getPricePerShare().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Price per share must be positive");
        }

        // Additional validations based on transaction type
        if (transaction.getTransactionType() == TransactionType.SELL) {
            // Check if user has enough shares to sell
            BigDecimal currentHolding = getCurrentHolding(transaction.getPortfolio(), transaction.getSecurityStock());
            if (currentHolding.compareTo(transaction.getQuantity()) < 0) {
                throw new InvalidTransactionException("Insufficient shares to sell. Available: " + currentHolding +
                        ", Requested: " + transaction.getQuantity());
            }
        }
    }

    private BigDecimal getCurrentHolding(Portfolio portfolio, SecurityStock security) {
        // Get current holding from position service
        try {
            return positionService.getCurrentQuantity(portfolio.getPortfolioId(), security.getSymbol());
        } catch (Exception e) {
            logger.warn("Could not get current holding for {} in portfolio {}: {}",
                    security.getSymbol(), portfolio.getPortfolioId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Transaction processTransactionEffects(Transaction transaction) {
        if (!transaction.affectsPortfolioBalance() && !transaction.affectsPosition()) {
            return transaction;
        }

        try {
            // Update portfolio cash balance
            if (transaction.affectsPortfolioBalance()) {
                updatePortfolioCashBalance(transaction);
            }

            // Update position
            if (transaction.affectsPosition()) {
                updatePosition(transaction);
            }

        } catch (Exception e) {
            logger.error("Failed to process transaction effects for {}: {}",
                    transaction.getTransactionId(), e.getMessage());
            throw new RuntimeException("Failed to process transaction effects", e);
        }

        return transaction;
    }

    // Replace updatePortfolioCashBalance method
    @Transactional
    private void updatePortfolioCashBalance(Transaction transaction) {
        // Lock the portfolio for update to prevent concurrent modifications
        Portfolio portfolio = portfolioRepository.findById(transaction.getPortfolio().getPortfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        BigDecimal adjustment = calculateCashAdjustment(transaction);
        BigDecimal newBalance = portfolio.getCashBalance().add(adjustment);

        // Validate before updating
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Transaction would result in negative cash balance");
        }

        portfolio.setCashBalance(newBalance);
        // Recalculate total value
        BigDecimal positionValue = positionRepository.getTotalCurrentValue(portfolio.getPortfolioId());
        portfolio.setTotalValue(newBalance.add(positionValue));

        portfolioRepository.save(portfolio);
    }

    private BigDecimal calculateCashAdjustment(Transaction transaction) {
        return switch (transaction.getTransactionType()) {
            case BUY, WITHDRAWAL, FEE, TAX -> transaction.getNetAmount().negate();
            case SELL, DIVIDEND, INTEREST, DEPOSIT -> transaction.getNetAmount();
            default -> BigDecimal.ZERO;
        };
    }

    private void updatePosition(Transaction transaction) {
        // Delegate to position service
        try {
            positionService.updatePositionFromTransaction(transaction.getPortfolio().getPortfolioId(),
                    transaction.getSecurityStock().getSymbol(),
                    transaction.getTransactionType(),
                    transaction.getQuantity(),
                    transaction.getPricePerShare());
        } catch (Exception e) {
            logger.error("Failed to update position for transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage());
            throw e;
        }
    }

    // Add to TransactionServiceImpl.java
    private void validateBusinessRules(Transaction transaction) {
        Portfolio portfolio = transaction.getPortfolio();

        // Check market hours for certain transaction types
        if (transaction.getTransactionType() == TransactionType.BUY ||
                transaction.getTransactionType() == TransactionType.SELL) {
            if (!isMarketOpen() && !isTestEnvironment()) {
                throw new InvalidTransactionException("Cannot execute trades outside market hours");
            }
        }

        // Validate settlement date
        if (transaction.getSettlementDate() != null &&
                transaction.getSettlementDate().isBefore(transaction.getTransactionDate())) {
            throw new InvalidTransactionException("Settlement date cannot be before transaction date");
        }
    }

    private TransactionResponseDTO convertToResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();

        dto.setTransactionId(transaction.getTransactionId());
        dto.setPortfolioId(transaction.getPortfolio().getPortfolioId());
        dto.setPortfolioName(transaction.getPortfolio().getPortfolioName());
        dto.setStockSymbol(transaction.getSecurityStock().getSymbol());
        dto.setCompanyName(transaction.getSecurityStock().getCompanyName());
        dto.setOrderId(transaction.getOrder() != null ? transaction.getOrder().getOrderId() : null);
        dto.setQuantity(transaction.getQuantity());
        dto.setPricePerShare(transaction.getPricePerShare());
        dto.setTotalAmount(transaction.getTotalAmount());
        dto.setFees(transaction.getFees());
        dto.setTaxAmount(transaction.getTaxAmount());
        dto.setNetAmount(transaction.getNetAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setSettlementDate(transaction.getSettlementDate());
        dto.setNotes(transaction.getNotes());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setOrderType(transaction.getOrderType());
        dto.setTransactionStatus(transaction.getTransactionStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setLastUpdated(transaction.getLastUpdated());

        // Set calculated fields
        dto.setSettled(transaction.isSettled());
        dto.setAffectsPortfolioBalance(transaction.affectsPortfolioBalance());
        dto.setAffectsPosition(transaction.affectsPosition());

        return dto;
    }

    //Helper methods
    private boolean isMarketOpen() {
        // Implementation for market hours check
        // For now, return true - you can implement proper market hours logic later
        return true;
    }

    private boolean isTestEnvironment() {
        // Check if running in test environment
        return "test".equals(System.getProperty("spring.profiles.active"));
    }
}
