package com.elitecart.backend.controller;

import com.elitecart.backend.dto.cart.AddCartItemRequest;
import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.cart.UpdateCartItemRequest;
import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.security.UserPrincipal;
import com.elitecart.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current customer's shopping cart: add/update/remove items, view
 * computed totals (subtotal, tax, shipping, grand total), and clear cart.
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart APIs for the current customer")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get the current user's cart with computed totals")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(principal.getId())));
    }

    @Operation(summary = "Add a product to the cart (increments quantity if it's already there)")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody AddCartItemRequest request) {
        CartResponse response = cartService.addItem(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
    }

    @Operation(summary = "Set the exact quantity for a cart item (increase or decrease)")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Long itemId,
                                                                 @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateItem(principal.getId(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", response));
    }

    @Operation(summary = "Remove a single item from the cart")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Long itemId) {
        CartResponse response = cartService.removeItem(principal.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", response));
    }

    @Operation(summary = "Clear the entire cart")
    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartResponse response = cartService.clearCart(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", response));
    }
}
