package com.elitecart.backend.service;

import com.elitecart.backend.dto.cart.AddCartItemRequest;
import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.cart.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addItem(Long userId, AddCartItemRequest request);
    CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request);
    CartResponse removeItem(Long userId, Long itemId);
    CartResponse clearCart(Long userId);
}
