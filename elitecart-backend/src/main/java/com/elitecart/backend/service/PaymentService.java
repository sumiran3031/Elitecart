package com.elitecart.backend.service;

import com.elitecart.backend.entity.Order;
import com.elitecart.backend.entity.Payment;

public interface PaymentService {
    /** Processes a mock payment for the given order and returns the persisted Payment. */
    Payment processPayment(Order order, String paymentMethod);

    /** Mock refund — flips a SUCCESS payment to REFUNDED. */
    Payment refund(Long orderId);
}
