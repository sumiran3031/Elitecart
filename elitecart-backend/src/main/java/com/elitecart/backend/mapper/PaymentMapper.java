package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.payment.PaymentResponse;
import com.elitecart.backend.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    default PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .method(payment.getMethod() != null ? payment.getMethod().name() : null)
                .status(payment.getStatus().name())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
