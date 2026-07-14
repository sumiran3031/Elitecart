package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.order.CheckoutRequest;
import com.elitecart.backend.dto.order.OrderResponse;
import com.elitecart.backend.entity.Address;
import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.CartItem;
import com.elitecart.backend.entity.Order;
import com.elitecart.backend.entity.OrderItem;
import com.elitecart.backend.entity.OrderStatus;
import com.elitecart.backend.entity.Payment;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.OrderMapper;
import com.elitecart.backend.repository.AddressRepository;
import com.elitecart.backend.repository.CartRepository;
import com.elitecart.backend.repository.OrderRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.service.EmailService;
import com.elitecart.backend.service.OrderService;
import com.elitecart.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Business logic for the Order module: checkout (cart -> order, stock
 * deduction, mock payment, confirmation email, cart clearing), order
 * history/detail, customer-initiated cancellation, and admin status
 * transitions (with shipped/delivered emails).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal FLAT_SHIPPING_FEE = new BigDecimal("9.99");
    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for this user"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        Address shippingAddress = getOwnedAddress(userId, request.getShippingAddressId());
        Address billingAddress = getOwnedAddress(userId, request.getBillingAddressId());

        // Validate stock and build order items up-front so we fail before touching anything
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() == null || product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + product.getName() + "'");
            }
            BigDecimal unitPrice = effectivePrice(product);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : FLAT_SHIPPING_FEE;
        BigDecimal grandTotal = subtotal.add(tax).add(shipping);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .subtotal(subtotal)
                .tax(tax)
                .shippingFee(shipping)
                .discount(BigDecimal.ZERO)
                .grandTotal(grandTotal)
                .status(OrderStatus.PENDING)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Deduct stock + track units sold
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setUnitsSold((product.getUnitsSold() == null ? 0L : product.getUnitsSold()) + cartItem.getQuantity());
            productRepository.save(product);
        }

        Payment payment = paymentService.processPayment(savedOrder, request.getPaymentMethod());
        savedOrder.setPayment(payment);
        savedOrder.setStatus(payment.getStatus() == Payment.PaymentStatus.SUCCESS ? OrderStatus.CONFIRMED : OrderStatus.PENDING);
        Order finalOrder = orderRepository.save(savedOrder);

        // Cart is only cleared after a successful order + payment attempt
        cart.getItems().clear();
        cartRepository.save(cart);

        String summaryHtml = buildOrderSummaryHtml(finalOrder);
        emailService.sendOrderConfirmationEmail(user, finalOrder.getOrderNumber(), summaryHtml);

        log.info("Order {} placed for user {}", finalOrder.getOrderNumber(), user.getEmail());
        return orderMapper.toResponse(finalOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        return orderMapper.toResponse(getOwnedOrder(userId, orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(Long userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        assertOwnership(order, userId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId, String reason) {
        Order order = getOwnedOrder(userId, orderId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new BadRequestException("Order in status " + order.getStatus() + " can no longer be cancelled");
        }

        // Restock items
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                product.setUnitsSold(Math.max(0, product.getUnitsSold() - item.getQuantity()));
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledReason(reason);
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = parseStatus(status);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPED) {
            emailService.sendOrderShippedEmail(order.getUser(), order.getOrderNumber());
        } else if (newStatus == OrderStatus.DELIVERED) {
            emailService.sendOrderDeliveredEmail(order.getUser(), order.getOrderNumber());
        }

        return orderMapper.toResponse(saved);
    }

    private Address getOwnedAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("This address does not belong to the current user");
        }
        return address;
    }

    private Order getOwnedOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        assertOwnership(order, userId);
        return order;
    }

    private void assertOwnership(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("This order does not belong to the current user");
        }
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
    }

    private BigDecimal effectivePrice(Product product) {
        if (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                && product.getDiscountPrice().compareTo(product.getPrice()) < 0) {
            return product.getDiscountPrice();
        }
        return product.getPrice();
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    private String buildOrderSummaryHtml(Order order) {
        StringBuilder sb = new StringBuilder("<table style='width:100%;border-collapse:collapse;'>");
        for (OrderItem item : order.getItems()) {
            sb.append("<tr><td style='padding:6px 0;'>").append(item.getProductName())
                    .append(" x ").append(item.getQuantity())
                    .append("</td><td style='padding:6px 0;text-align:right;'>$")
                    .append(item.getLineTotal()).append("</td></tr>");
        }
        sb.append("<tr><td style='padding-top:10px;font-weight:600;'>Grand Total</td>")
                .append("<td style='padding-top:10px;text-align:right;font-weight:600;'>$")
                .append(order.getGrandTotal()).append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }
}
