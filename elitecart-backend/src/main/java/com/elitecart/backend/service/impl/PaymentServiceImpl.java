package com.elitecart.backend.service.impl;

import com.elitecart.backend.entity.Order;
import com.elitecart.backend.entity.Payment;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.repository.PaymentRepository;
import com.elitecart.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock payment gateway. Simulates a real processor: generates a transaction
 * id, "charges" the order total, and almost always succeeds — COD orders are
 * marked PENDING until delivery, everything else is SUCCESS immediately.
 * Swap this implementation for a real Stripe/Razorpay integration later
 * without touching OrderService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment processPayment(Order order, String paymentMethod) {
        Payment.PaymentMethod method = parseMethod(paymentMethod);
        String transactionId = "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        Payment.PaymentStatus status = (method == Payment.PaymentMethod.COD)
                ? Payment.PaymentStatus.PENDING
                : Payment.PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .order(order)
                .transactionId(transactionId)
                .amount(order.getGrandTotal())
                .method(method)
                .status(status)
                .paidAt(status == Payment.PaymentStatus.SUCCESS ? LocalDateTime.now() : null)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Mock payment processed: {} for order {} -> status {}", transactionId, order.getOrderNumber(), status);
        return saved;
    }

    @Override
    @Transactional
    public Payment refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded. Current status: " + payment.getStatus());
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private Payment.PaymentMethod parseMethod(String method) {
        try {
            return Payment.PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Invalid payment method. Must be one of CARD, UPI, NET_BANKING, COD, WALLET");
        }
    }
}
