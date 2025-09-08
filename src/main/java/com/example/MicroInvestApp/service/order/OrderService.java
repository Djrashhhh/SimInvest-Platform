package com.example.MicroInvestApp.service.order;

import com.example.MicroInvestApp.domain.enums.OrderSide;
import com.example.MicroInvestApp.domain.enums.OrderStatus;
import com.example.MicroInvestApp.dto.orders.OrderRequestDTO;
import com.example.MicroInvestApp.dto.orders.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    // Order Management
    OrderResponseDTO createOrder(OrderRequestDTO orderRequest);
    OrderResponseDTO executeOrder(Long orderId);
    OrderResponseDTO cancelOrder(Long orderId, String reason);
    Optional<OrderResponseDTO> getOrderById(Long orderId);

    // Order Queries - Portfolio Based
    List<OrderResponseDTO> getOrdersByPortfolio(Long portfolioId);
    Page<OrderResponseDTO> getOrdersByPortfolio(Long portfolioId, Pageable pageable);
    List<OrderResponseDTO> getActiveOrdersByPortfolio(Long portfolioId);

    // Order Queries - Status Based
    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    // Order Queries - Portfolio and Side Based
    List<OrderResponseDTO> getOrdersByPortfolioAndOrderSide(Long portfolioId, OrderSide orderSide);

    // Order Queries - Security Based
    List<OrderResponseDTO> getOrdersByPortfolioAndSecurity(Long portfolioId, String stockSymbol);

    // Order Queries - Date Range Based
    List<OrderResponseDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<OrderResponseDTO> getOrdersByPortfolioAndDateRange(Long portfolioId, LocalDateTime startDate, LocalDateTime endDate);

    // Order Processing
    void processExpiredOrders();
    List<OrderResponseDTO> getActiveOrders();

    // Order Statistics
    Map<String, Object> getOrderStatsByPortfolio(Long portfolioId);

    // Order Validation
    boolean validateOrder(OrderRequestDTO orderRequest);
    boolean canCancelOrder(Long orderId);
}