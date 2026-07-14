package com.elitecart.backend.controller;

import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.dto.order.CancelOrderRequest;
import com.elitecart.backend.dto.order.CheckoutRequest;
import com.elitecart.backend.dto.order.OrderResponse;
import com.elitecart.backend.security.UserPrincipal;
import com.elitecart.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-facing order endpoints: checkout (cart -> order), order history,
 * order detail, and cancellation.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Checkout and order history APIs for the current customer")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Checkout: convert the current cart into an order and process (mock) payment")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@AuthenticationPrincipal UserPrincipal principal,
                                                                @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.checkout(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order placed successfully", response));
    }

    @Operation(summary = "Get paginated order history for the current user")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersForUser(principal.getId(), pageable)));
    }

    @Operation(summary = "Get a single order by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(principal.getId(), id)));
    }

    @Operation(summary = "Get a single order by order number")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getByNumber(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByNumber(principal.getId(), orderNumber)));
    }

    @Operation(summary = "Cancel an order (only while PENDING or CONFIRMED)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable Long id,
                                                              @Valid @RequestBody CancelOrderRequest request) {
        OrderResponse response = orderService.cancelOrder(principal.getId(), id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }
}
