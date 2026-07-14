package com.elitecart.backend.controller;

import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.dto.wishlist.AddWishlistItemRequest;
import com.elitecart.backend.dto.wishlist.WishlistResponse;
import com.elitecart.backend.security.UserPrincipal;
import com.elitecart.backend.service.WishlistService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current customer's wishlist: add/remove products and move a wishlist
 * item straight into the cart.
 */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Wishlist", description = "Wishlist APIs for the current customer")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get the current user's wishlist")
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getWishlist(principal.getId())));
    }

    @Operation(summary = "Add a product to the wishlist")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItem(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody AddWishlistItemRequest request) {
        WishlistResponse response = wishlistService.addItem(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to wishlist", response));
    }

    @Operation(summary = "Remove an item from the wishlist")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeItem(@AuthenticationPrincipal UserPrincipal principal,
                                                                     @PathVariable Long itemId) {
        WishlistResponse response = wishlistService.removeItem(principal.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist", response));
    }

    @Operation(summary = "Move a wishlist item into the cart")
    @PostMapping("/items/{itemId}/move-to-cart")
    public ResponseEntity<ApiResponse<CartResponse>> moveToCart(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @PathVariable Long itemId) {
        CartResponse response = wishlistService.moveToCart(principal.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item moved to cart", response));
    }
}
