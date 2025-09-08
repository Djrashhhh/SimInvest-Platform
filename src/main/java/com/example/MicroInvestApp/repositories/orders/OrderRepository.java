package com.example.MicroInvestApp.repositories.orders;

import com.example.MicroInvestApp.domain.enums.OrderSide;
import com.example.MicroInvestApp.domain.enums.OrderStatus;
import com.example.MicroInvestApp.domain.market.SecurityStock;
import com.example.MicroInvestApp.domain.orders.Order;
import com.example.MicroInvestApp.domain.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by portfolio
    List<Order> findByPortfolioOrderByOrderPlacedDateDesc(Portfolio portfolio);

    // Find orders by status
    List<Order> findByOrderStatusOrderByOrderPlacedDateDesc(OrderStatus status);

    // Find orders by portfolio with pagination
    Page<Order> findByPortfolioOrderByOrderPlacedDateDesc(Portfolio portfolio, Pageable pageable);

    // Find active orders (PENDING or PARTIALLY_FILLED)
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN ('PENDING', 'PARTIALLY_FILLED') ORDER BY o.orderPlacedDate DESC")
    List<Order> findActiveOrders();

    // Find active orders for a specific portfolio
    @Query("SELECT o FROM Order o WHERE o.portfolio = :portfolio AND o.orderStatus IN ('PENDING', 'PARTIALLY_FILLED') ORDER BY o.orderPlacedDate DESC")
    List<Order> findActiveOrdersByPortfolio(@Param("portfolio") Portfolio portfolio);

    // Find orders by portfolio and status
    List<Order> findByPortfolioAndOrderStatusOrderByOrderPlacedDateDesc(Portfolio portfolio, OrderStatus status);

    // Find orders by portfolio and order side
    List<Order> findByPortfolioAndOrderSideOrderByOrderPlacedDateDesc(Portfolio portfolio, OrderSide orderSide);

    // Find orders by security
    List<Order> findBySecurityStockOrderByOrderPlacedDateDesc(SecurityStock securityStock);

    // Find orders by portfolio and security
    List<Order> findByPortfolioAndSecurityStockOrderByOrderPlacedDateDesc(Portfolio portfolio, SecurityStock securityStock);

    // Find orders within date range
    @Query("SELECT o FROM Order o WHERE o.orderPlacedDate >= :startDate AND o.orderPlacedDate <= :endDate ORDER BY o.orderPlacedDate DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find orders by portfolio within date range
    @Query("SELECT o FROM Order o WHERE o.portfolio = :portfolio AND o.orderPlacedDate >= :startDate AND o.orderPlacedDate <= :endDate ORDER BY o.orderPlacedDate DESC")
    List<Order> findOrdersByPortfolioAndDateRange(@Param("portfolio") Portfolio portfolio,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Find expired orders that need to be cancelled
    @Query("SELECT o FROM Order o WHERE o.expiryDate <= :currentTime AND o.orderStatus IN ('PENDING', 'PARTIALLY_FILLED')")
    List<Order> findExpiredOrders(@Param("currentTime") LocalDateTime currentTime);

    // Count orders by status for a portfolio
    long countByPortfolioAndOrderStatus(Portfolio portfolio, OrderStatus status);

    // Count orders by portfolio and order side
    long countByPortfolioAndOrderSide(Portfolio portfolio, OrderSide orderSide);

    // Get order statistics for portfolio
    @Query("SELECT " +
            "COUNT(o) as totalOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'FILLED' THEN 1 END) as filledOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'PENDING' THEN 1 END) as pendingOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'CANCELLED' THEN 1 END) as cancelledOrders " +
            "FROM Order o WHERE o.portfolio = :portfolio")
    Object[] getOrderStatsByPortfolio(@Param("portfolio") Portfolio portfolio);

    // Additional useful queries for enhanced functionality

    // Find orders by multiple statuses
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses ORDER BY o.orderPlacedDate DESC")
    List<Order> findByOrderStatusInOrderByOrderPlacedDateDesc(@Param("statuses") List<OrderStatus> statuses);

    // Find orders by portfolio and multiple statuses
    @Query("SELECT o FROM Order o WHERE o.portfolio = :portfolio AND o.orderStatus IN :statuses ORDER BY o.orderPlacedDate DESC")
    List<Order> findByPortfolioAndOrderStatusInOrderByOrderPlacedDateDesc(@Param("portfolio") Portfolio portfolio,
                                                                          @Param("statuses") List<OrderStatus> statuses);

    // Find orders by order side
    List<Order> findByOrderSideOrderByOrderPlacedDateDesc(OrderSide orderSide);

    // Find orders by portfolio, order side and status
    List<Order> findByPortfolioAndOrderSideAndOrderStatusOrderByOrderPlacedDateDesc(Portfolio portfolio,
                                                                                    OrderSide orderSide,
                                                                                    OrderStatus status);

    // Find orders by security and order side
    List<Order> findBySecurityStockAndOrderSideOrderByOrderPlacedDateDesc(SecurityStock securityStock, OrderSide orderSide);

    // Find orders by portfolio, security and order side
    List<Order> findByPortfolioAndSecurityStockAndOrderSideOrderByOrderPlacedDateDesc(Portfolio portfolio,
                                                                                      SecurityStock securityStock,
                                                                                      OrderSide orderSide);

    // Find orders by date range and order side
    @Query("SELECT o FROM Order o WHERE o.orderPlacedDate >= :startDate AND o.orderPlacedDate <= :endDate AND o.orderSide = :orderSide ORDER BY o.orderPlacedDate DESC")
    List<Order> findOrdersByDateRangeAndOrderSide(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("orderSide") OrderSide orderSide);

    // Find orders with fees greater than specified amount
    @Query("SELECT o FROM Order o WHERE o.totalFees >= :minFees ORDER BY o.totalFees DESC")
    List<Order> findOrdersWithFeesGreaterThan(@Param("minFees") java.math.BigDecimal minFees);

    // Find large orders above specified quantity threshold
    @Query("SELECT o FROM Order o WHERE o.quantity >= :minQuantity ORDER BY o.quantity DESC")
    List<Order> findLargeOrders(@Param("minQuantity") java.math.BigDecimal minQuantity);

    // Get order count by order side for portfolio
    @Query("SELECT o.orderSide, COUNT(o) FROM Order o WHERE o.portfolio = :portfolio GROUP BY o.orderSide")
    List<Object[]> getOrderCountByOrderSideForPortfolio(@Param("portfolio") Portfolio portfolio);

    // Get order statistics by order side
    @Query("SELECT o.orderSide, " +
            "COUNT(o) as totalOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'FILLED' THEN 1 END) as filledOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'PENDING' THEN 1 END) as pendingOrders, " +
            "COUNT(CASE WHEN o.orderStatus = 'CANCELLED' THEN 1 END) as cancelledOrders " +
            "FROM Order o WHERE o.portfolio = :portfolio " +
            "GROUP BY o.orderSide")
    List<Object[]> getOrderStatsByOrderSideForPortfolio(@Param("portfolio") Portfolio portfolio);
}