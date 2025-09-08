package com.example.MicroInvestApp.impl.orders;

import com.example.MicroInvestApp.dto.orders.TransactionRequestDTO;
import com.example.MicroInvestApp.dto.orders.TransactionResponseDTO;
import com.example.MicroInvestApp.exception.InsufficientFundsException;
import com.example.MicroInvestApp.exception.Orders.InvalidOrderException;
import com.example.MicroInvestApp.exception.Orders.OrderNotFoundException;
import com.example.MicroInvestApp.exception.portfolio.PortfolioNotFoundException;
import com.example.MicroInvestApp.service.order.OrderService;
import com.example.MicroInvestApp.domain.orders.Order;
import com.example.MicroInvestApp.domain.orders.Transaction;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.enums.*;
import com.example.MicroInvestApp.dto.orders.OrderRequestDTO;
import com.example.MicroInvestApp.dto.orders.OrderResponseDTO;
import com.example.MicroInvestApp.repositories.orders.OrderRepository;
import com.example.MicroInvestApp.repositories.portfolio.PortfolioRepository;
import com.example.MicroInvestApp.repositories.market.SecurityStockRepository;
import com.example.MicroInvestApp.service.order.TransactionService;
import com.example.MicroInvestApp.service.portfolio.PositionService;
import com.example.MicroInvestApp.service.market.MarketDataService;
import com.example.MicroInvestApp.service.market.SecurityCreationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
//import io.micrometer.core.instrument.MeterRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final SecurityStockRepository securityStockRepository;
    private final TransactionService transactionService;
    private final PositionService positionService;
    private final MarketDataService marketDataService;
    private final SecurityCreationService securityCreationService;
    //private final MeterRegistry meterRegistry;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            PortfolioRepository portfolioRepository,
                            SecurityStockRepository securityStockRepository,
                            TransactionService transactionService,
                            PositionService positionService,
                            MarketDataService marketDataService,
                            SecurityCreationService securityCreationService
                            ) {
        this.orderRepository = orderRepository;
        this.portfolioRepository = portfolioRepository;
        this.securityStockRepository = securityStockRepository;
        this.transactionService = transactionService;
        this.positionService = positionService;
        this.marketDataService = marketDataService;
        this.securityCreationService = securityCreationService;

    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequest) {
        logger.info("Creating {} order for portfolio {} - {} shares of {}",
                orderRequest.getOrderSide(), orderRequest.getPortfolioId(),
                orderRequest.getQuantity(), orderRequest.getStockSymbol());

        // Validate the order request
        if (!validateOrder(orderRequest)) {
            throw new InvalidOrderException("Order validation failed");
        }

        // Find portfolio
        Portfolio portfolio = portfolioRepository.findById(orderRequest.getPortfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + orderRequest.getPortfolioId()));

        // Find or create security
        SecurityStock security = findOrCreateSecurity(orderRequest.getStockSymbol());

        // Create order with OrderSide
        Order order = new Order(portfolio, security, orderRequest.getQuantity(),
                orderRequest.getOrderPrice(), orderRequest.getOrderType(),
                orderRequest.getOrderSide(), orderRequest.getNotes());

        // Set expiry date if provided
        if (orderRequest.getExpiryDate() != null) {
            order.setExpiryDate(orderRequest.getExpiryDate());
        }

        // Check business rules based on order side
        if (orderRequest.getOrderSide() == OrderSide.BUY) {
            checkSufficientFundsForBuyOrder(portfolio, order.getEstimatedTotal());
        } else if (orderRequest.getOrderSide() == OrderSide.SELL) {
            checkSufficientSharesForSellOrder(portfolio, security, orderRequest.getQuantity());
        }

        // Save order
        order = orderRepository.save(order);

        // Try to execute market orders immediately
        if (orderRequest.getOrderType() == OrderType.MARKET) {
            try {
                order = executeMarketOrder(order);
            } catch (Exception e) {
                logger.error("Failed to execute market order {}: {}", order.getOrderId(), e.getMessage());
                order.markAsFailed("Market execution failed: " + e.getMessage());
                order = orderRepository.save(order);
            }
        }

        logger.info("Order created successfully: {}", order.getOrderId());
        return convertToResponseDTO(order);
    }

    @Override
    public OrderResponseDTO executeOrder(Long orderId) {
        logger.info("Executing order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.isActive()) {
            throw new InvalidOrderException("Order is not active and cannot be executed");
        }

        try {
            // Re-validate business rules before execution
            if (order.getOrderSide() == OrderSide.BUY) {
                checkSufficientFundsForBuyOrder(order.getPortfolio(), order.getEstimatedTotal());
            } else if (order.getOrderSide() == OrderSide.SELL) {
                checkSufficientSharesForSellOrder(order.getPortfolio(), order.getSecurityStock(), order.getQuantity());
            }

            // Execute the order based on type
            if (order.getOrderType() == OrderType.MARKET) {
                order = executeMarketOrder(order);
            } else if (order.getOrderType() == OrderType.LIMIT) {
                order = executeLimitOrder(order);
            }

            order = orderRepository.save(order);
            logger.info("Order {} executed successfully", orderId);

        } catch (Exception e) {
            logger.error("Failed to execute order {}: {}", orderId, e.getMessage());
            order.markAsFailed("Execution failed: " + e.getMessage());
            order = orderRepository.save(order);
        }

        return convertToResponseDTO(order);
    }

    @Override
    public OrderResponseDTO cancelOrder(Long orderId, String reason) {
        logger.info("Cancelling order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.canBeCancelled()) {
            throw new InvalidOrderException("Order cannot be cancelled in current status: " + order.getOrderStatus());
        }

        order.markAsCancelled(reason);
        order = orderRepository.save(order);

        logger.info("Order {} cancelled successfully", orderId);
        return convertToResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponseDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return orderRepository.findByPortfolioOrderByOrderPlacedDateDesc(portfolio)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByPortfolio(Long portfolioId, Pageable pageable) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return orderRepository.findByPortfolioOrderByOrderPlacedDateDesc(portfolio, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrdersByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return orderRepository.findActiveOrdersByPortfolio(portfolio)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatusOrderByOrderPlacedDateDesc(status)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // New method: Get orders by portfolio and order side
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPortfolioAndOrderSide(Long portfolioId, OrderSide orderSide) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return orderRepository.findByPortfolioAndOrderSideOrderByOrderPlacedDateDesc(portfolio, orderSide)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPortfolioAndSecurity(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        SecurityStock security = securityStockRepository.findBySymbol(stockSymbol)
                .orElse(null);

        if (security == null) {
            return Collections.emptyList();
        }

        return orderRepository.findByPortfolioAndSecurityStockOrderByOrderPlacedDateDesc(portfolio, security)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersByDateRange(startDate, endDate)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByPortfolioAndDateRange(Long portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        return orderRepository.findOrdersByPortfolioAndDateRange(portfolio, startDate, endDate)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void processExpiredOrders() {
        logger.info("Processing expired orders");

        List<Order> expiredOrders = orderRepository.findExpiredOrders(LocalDateTime.now());

        for (Order order : expiredOrders) {
            try {
                order.markAsCancelled("Order expired");
                orderRepository.save(order);
                logger.info("Cancelled expired order: {}", order.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to cancel expired order {}: {}", order.getOrderId(), e.getMessage());
            }
        }

        logger.info("Processed {} expired orders", expiredOrders.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getActiveOrders() {
        return orderRepository.findActiveOrders()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatsByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        Object[] stats = orderRepository.getOrderStatsByPortfolio(portfolio);

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", stats[0]);
        result.put("filledOrders", stats[1]);
        result.put("pendingOrders", stats[2]);
        result.put("cancelledOrders", stats[3]);

        // Add order side statistics
        Long buyOrders = orderRepository.countByPortfolioAndOrderSide(portfolio, OrderSide.BUY);
        Long sellOrders = orderRepository.countByPortfolioAndOrderSide(portfolio, OrderSide.SELL);

        result.put("buyOrders", buyOrders);
        result.put("sellOrders", sellOrders);

        return result;
    }

    // Replace the validateOrder method
    @Override
    public boolean validateOrder(OrderRequestDTO orderRequest) {
        // Basic validation
        if (orderRequest.getQuantity() == null || orderRequest.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid quantity: {}", orderRequest.getQuantity());
            return false;
        }

        // Validate order side is provided
        if (orderRequest.getOrderSide() == null) {
            logger.error("Order side is required");
            return false;
        }

        // Validate order type specific requirements
        if (orderRequest.getOrderType().requiresPrice() && orderRequest.getOrderPrice() == null) {
            logger.error("Order price is required for order type: {}", orderRequest.getOrderType());
            return false;
        }

        if (orderRequest.getOrderPrice() != null && orderRequest.getOrderPrice().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Invalid order price: {}", orderRequest.getOrderPrice());
            return false;
        }

        // For sell orders, check if user has sufficient shares
        if (orderRequest.getOrderSide() == OrderSide.SELL) {
            BigDecimal currentPosition = positionService.getCurrentQuantity(
                    orderRequest.getPortfolioId(),
                    orderRequest.getStockSymbol()
            );
            if (currentPosition.compareTo(orderRequest.getQuantity()) < 0) {
                logger.error("Insufficient shares for sell order. Available: {}, Requested: {}",
                        currentPosition, orderRequest.getQuantity());
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::canBeCancelled)
                .orElse(false);
    }

    // Enhanced helper methods with OrderSide support
    private SecurityStock findOrCreateSecurity(String symbol) {
        Optional<SecurityStock> existing = securityStockRepository.findBySymbol(symbol.toUpperCase());
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            logger.info("Security {} not found, creating new security", symbol);
            SecurityStock newSecurity = securityCreationService.createSecurityFromSymbol(symbol.toUpperCase());
            return newSecurity;
        } catch (Exception e) {
            throw new RuntimeException("Unable to find or create security: " + symbol, e);
        }
    }

    private void checkSufficientFundsForBuyOrder(Portfolio portfolio, BigDecimal requiredAmount) {
        if (portfolio.getCashBalance().compareTo(requiredAmount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for buy order. Required: " + requiredAmount +
                    ", Available: " + portfolio.getCashBalance());
        }
    }

    // Replace the checkSufficientSharesForSellOrder method
    private void checkSufficientSharesForSellOrder(Portfolio portfolio, SecurityStock security, BigDecimal quantityToSell) {
        // Get current position quantity (not value!)
        BigDecimal currentQuantity = positionService.getCurrentQuantity(portfolio.getPortfolioId(), security.getSymbol());

        if (currentQuantity.compareTo(quantityToSell) < 0) {
            throw new InvalidOrderException("Insufficient shares for sell order. Required: " + quantityToSell +
                    ", Available: " + currentQuantity + " shares of " + security.getSymbol());
        }
    }

    // ✅ REPLACE THE EXISTING TRANSACTION REQUEST CREATION WITH THIS:
    private Order executeMarketOrder(Order order) {
        // Get current market price
        BigDecimal currentPrice = order.getSecurityStock().getCurrentPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            SecurityStock updatedSecurity = marketDataService.updateCurrentPrice(order.getSecurityStock().getSymbol());
            currentPrice = updatedSecurity.getCurrentPrice();

            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Unable to get current market price for " + order.getSecurityStock().getSymbol());
            }
        }

        // Calculate total amount and fees
        BigDecimal totalAmount = order.getQuantity().multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fees = calculateOrderFees(totalAmount);

        // Determine transaction type based on order side
        TransactionType transactionType = order.getOrderSide() == OrderSide.BUY ?
                TransactionType.BUY : TransactionType.SELL;

        // ✅ CREATE TRANSACTION DTO WITH ORDER LINKAGE
        TransactionRequestDTO transactionRequest = new TransactionRequestDTO();
        transactionRequest.setPortfolioId(order.getPortfolio().getPortfolioId());
        transactionRequest.setStockSymbol(order.getSecurityStock().getSymbol());
        transactionRequest.setOrderId(order.getOrderId()); // ← ADD THIS LINE
        transactionRequest.setQuantity(order.getQuantity());
        transactionRequest.setPricePerShare(currentPrice.setScale(4, RoundingMode.HALF_UP));
        transactionRequest.setTransactionType(transactionType);
        transactionRequest.setFees(fees);
        transactionRequest.setNotes("Order execution: " + order.getOrderId());

        // Create transaction
        TransactionResponseDTO transaction = transactionService.createTransaction(transactionRequest);

        // Update order
        order.setFilledQuantity(order.getQuantity());
        order.setAverageFillPrice(currentPrice.setScale(4, RoundingMode.HALF_UP));
        order.setTotalFees(fees);
        order.markAsExecuted();

        logger.info("Market order {} executed: {} {} shares at ${}",
                order.getOrderId(), transactionType, order.getQuantity(), currentPrice);

        return order;
    }

    private Order executeLimitOrder(Order order) {
        BigDecimal currentPrice = order.getSecurityStock().getCurrentPrice();
        boolean shouldExecute = false;

        // Check execution conditions based on order side
        if (order.getOrderSide() == OrderSide.BUY) {
            // Buy limit: execute if current price <= limit price
            shouldExecute = currentPrice.compareTo(order.getOrderPrice()) <= 0;
        } else if (order.getOrderSide() == OrderSide.SELL) {
            // Sell limit: execute if current price >= limit price
            shouldExecute = currentPrice.compareTo(order.getOrderPrice()) >= 0;
        }

        if (shouldExecute) {
            logger.info("Limit order {} conditions met, executing at current price", order.getOrderId());
            return executeMarketOrder(order);
        } else {
            logger.debug("Limit order {} conditions not met, remaining pending", order.getOrderId());
            return order;
        }
    }

    private BigDecimal calculateOrderFees(BigDecimal totalAmount) {
        // Enhanced fee structure: 0.5% with minimum $1.00, maximum $50.00
        BigDecimal feeRate = new BigDecimal("0.005"); // 0.5%
        BigDecimal calculatedFee = totalAmount.multiply(feeRate);
        BigDecimal minimumFee = new BigDecimal("1.00");
        BigDecimal maximumFee = new BigDecimal("50.00");

        return calculatedFee.max(minimumFee).min(maximumFee).setScale(2, RoundingMode.HALF_UP);
    }

    // Add metrics tracking for order events
//    @EventListener
//    public void handleOrderCreated(OrderCreatedEvent event) {
//        meterRegistry.counter("orders.created",
//                        "side", event.getOrderSide().toString(),
//                        "type", event.getOrderType().toString())
//                .increment();
//    }
//
//    @EventListener
//    public void handleOrderExecuted(OrderExecutedEvent event) {
//        meterRegistry.timer("orders.execution.time")
//                .record(Duration.between(event.getCreatedTime(), event.getExecutedTime()));
//    }

    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setOrderId(order.getOrderId());
        dto.setPortfolioId(order.getPortfolio().getPortfolioId());
        dto.setPortfolioName(order.getPortfolio().getPortfolioName());
        dto.setStockSymbol(order.getSecurityStock().getSymbol());
        dto.setCompanyName(order.getSecurityStock().getCompanyName());
        dto.setQuantity(order.getQuantity());
        dto.setOrderPrice(order.getOrderPrice());
        dto.setEstimatedTotal(order.getEstimatedTotal());
        dto.setFilledQuantity(order.getFilledQuantity());
        dto.setAverageFillPrice(order.getAverageFillPrice());
        dto.setTotalFees(order.getTotalFees());
        dto.setOrderType(order.getOrderType());
        dto.setOrderSide(order.getOrderSide()); // Set the order side
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderPlacedDate(order.getOrderPlacedDate());
        dto.setOrderExecutedDate(order.getOrderExecutedDate());
        dto.setOrderCancelledDate(order.getOrderCancelledDate());
        dto.setExpiryDate(order.getExpiryDate());
        dto.setNotes(order.getNotes());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setLastUpdated(order.getLastUpdated());

        // Set calculated fields
        dto.setRemainingQuantity(order.getRemainingQuantity());
        dto.setCanBeCancelled(order.canBeCancelled());
        dto.setFullyFilled(order.isFullyFilled());
        dto.setPartiallyFilled(order.isPartiallyFilled());
        dto.setBuyOrder(order.isBuyOrder());
        dto.setSellOrder(order.isSellOrder());

        return dto;
    }

    private OrderRequestDTO convertToRequestDTO(Order order) {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setPortfolioId(order.getPortfolio().getPortfolioId());
        dto.setStockSymbol(order.getSecurityStock().getSymbol());
        dto.setQuantity(order.getQuantity());
        dto.setOrderPrice(order.getOrderPrice());
        dto.setOrderType(order.getOrderType());
        dto.setOrderSide(order.getOrderSide()); // Set the order side
        dto.setExpiryDate(order.getExpiryDate());
        dto.setNotes(order.getNotes());
        return dto;
    }
}