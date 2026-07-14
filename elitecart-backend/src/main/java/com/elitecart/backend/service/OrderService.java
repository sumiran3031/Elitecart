package com.elitecart.backend.service;

import com.elitecart.backend.dto.order.CheckoutRequest;
import com.elitecart.backend.dto.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse checkout(Long userId, CheckoutRequest request);
    Page<OrderResponse> getOrdersForUser(Long userId, Pageable pageable);
    OrderResponse getOrderById(Long userId, Long orderId);
    OrderResponse getOrderByNumber(Long userId, String orderNumber);
    OrderResponse cancelOrder(Long userId, Long orderId, String reason);

    // Admin operations
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, String status);
}
