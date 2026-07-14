package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.order.OrderItemResponse;
import com.elitecart.backend.dto.order.OrderResponse;
import com.elitecart.backend.entity.Order;
import com.elitecart.backend.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kept as a plain Spring component (rather than a MapStruct interface) since
 * it composes AddressMapper and PaymentMapper output for the nested response.
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final AddressMapper addressMapper;
    private final PaymentMapper paymentMapper;

    public OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getGrandTotal())
                .cancelledReason(order.getCancelledReason())
                .shippingAddress(addressMapper.toResponse(order.getShippingAddress()))
                .billingAddress(addressMapper.toResponse(order.getBillingAddress()))
                .items(items)
                .payment(paymentMapper.toResponse(order.getPayment()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
