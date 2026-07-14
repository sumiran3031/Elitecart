package com.elitecart.backend.service;

import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.wishlist.AddWishlistItemRequest;
import com.elitecart.backend.dto.wishlist.WishlistResponse;

public interface WishlistService {
    WishlistResponse getWishlist(Long userId);
    WishlistResponse addItem(Long userId, AddWishlistItemRequest request);
    WishlistResponse removeItem(Long userId, Long itemId);
    CartResponse moveToCart(Long userId, Long itemId);
}
