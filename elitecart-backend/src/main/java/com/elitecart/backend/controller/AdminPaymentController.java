package com.elitecart.backend.controller;

import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.dto.payment.PaymentResponse;
import com.elitecart.backend.entity.Payment;
import com.elitecart.backend.mapper.PaymentMapper;
import com.elitecart.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only mock refund endpoint. Routed under /admin/** which
 * SecurityConfig restricts to ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Payments", description = "Payment/refund management APIs (ADMIN only)")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Operation(summary = "Issue a mock refund for a successfully paid order")
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable Long orderId) {
        Payment refunded = paymentService.refund(orderId);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", paymentMapper.toResponse(refunded)));
    }
}
