package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.cart.AddCartItemRequest;
import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.wishlist.AddWishlistItemRequest;
import com.elitecart.backend.dto.wishlist.WishlistResponse;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.entity.Wishlist;
import com.elitecart.backend.entity.WishlistItem;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.WishlistMapper;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.repository.WishlistItemRepository;
import com.elitecart.backend.repository.WishlistRepository;
import com.elitecart.backend.service.CartService;
import com.elitecart.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {
        return wishlistMapper.toResponse(getOrCreateWishlist(userId));
    }

    @Override
    @Transactional
    public WishlistResponse addItem(Long userId, AddWishlistItemRequest request) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), product.getId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("This product is already in your wishlist");
                });

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
        wishlist.getItems().add(item);
        wishlistItemRepository.save(item);

        return wishlistMapper.toResponse(wishlistRepository.findById(wishlist.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public WishlistResponse removeItem(Long userId, Long itemId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        WishlistItem item = getOwnedItem(wishlist, itemId);
        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);

        return wishlistMapper.toResponse(wishlistRepository.findById(wishlist.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse moveToCart(Long userId, Long itemId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        WishlistItem item = getOwnedItem(wishlist, itemId);
        Long productId = item.getProduct().getId();

        CartResponse cartResponse = cartService.addItem(userId, AddCartItemRequest.builder()
                .productId(productId)
                .quantity(1)
                .build());

        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);

        return cartResponse;
    }

    private Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found for this user"));
    }

    private WishlistItem getOwnedItem(Wishlist wishlist, Long itemId) {
        return wishlist.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found with id: " + itemId));
    }
}
