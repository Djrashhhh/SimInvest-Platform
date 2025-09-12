package com.example.MicroInvestApp.service.order;

import com.example.MicroInvestApp.domain.enums.TransactionStatus;
import com.example.MicroInvestApp.domain.enums.TransactionType;
import com.example.MicroInvestApp.dto.orders.TransactionRequestDTO;
import com.example.MicroInvestApp.dto.orders.TransactionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionService {

    // Transaction Management
    TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequest);
    TransactionResponseDTO processTransaction(Long transactionId);
    TransactionResponseDTO cancelTransaction(Long transactionId, String reason);
    Optional<TransactionResponseDTO> getTransactionById(Long transactionId);

    // Transaction Queries
    List<TransactionResponseDTO> getTransactionsByPortfolio(Long portfolioId);
    Page<TransactionResponseDTO> getTransactionsByPortfolio(Long portfolioId, Pageable pageable);
    List<TransactionResponseDTO> getTransactionsByOrder(Long orderId);
    List<TransactionResponseDTO> getTransactionsByStatus(TransactionStatus status);
    List<TransactionResponseDTO> getTransactionsByType(TransactionType type);
    List<TransactionResponseDTO> getTransactionsByPortfolioAndSecurity(Long portfolioId, String stockSymbol);
    List<TransactionResponseDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<TransactionResponseDTO> getRecentTransactions(Long portfolioId, int limit);

    // Transaction Analytics
    BigDecimal getTotalQuantityBought(Long portfolioId, String stockSymbol);
    BigDecimal getTotalQuantitySold(Long portfolioId, String stockSymbol);
    BigDecimal getAverageCostBasis(Long portfolioId, String stockSymbol);
    BigDecimal getTotalDividendIncome(Long portfolioId);
    BigDecimal getTotalFeesPaid(Long portfolioId);
    Map<String, Object> getTransactionStatsByPortfolio(Long portfolioId);

    // Settlement Processing
    void processUnsettledTransactions();
    List<TransactionResponseDTO> getUnsettledTransactions();
}
