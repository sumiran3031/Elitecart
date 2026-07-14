package com.elitecart.backend.repository;

import com.elitecart.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM Order o WHERE o.status <> com.elitecart.backend.entity.OrderStatus.CANCELLED")
    BigDecimal sumRevenue();

    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, "
            + "COALESCE(SUM(grand_total), 0) AS revenue, COUNT(*) AS orderCount "
            + "FROM orders WHERE status <> 'CANCELLED' AND created_at >= :from "
            + "GROUP BY month ORDER BY month", nativeQuery = true)
    List<Object[]> findMonthlySales(@Param("from") LocalDateTime from);

    @Query(value = "SELECT c.name AS categoryName, COALESCE(SUM(oi.line_total), 0) AS revenue "
            + "FROM order_items oi "
            + "JOIN products p ON oi.product_id = p.id "
            + "JOIN categories c ON p.category_id = c.id "
            + "JOIN orders o ON oi.order_id = o.id "
            + "WHERE o.status <> 'CANCELLED' "
            + "GROUP BY c.name ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> findCategorySales();
}
