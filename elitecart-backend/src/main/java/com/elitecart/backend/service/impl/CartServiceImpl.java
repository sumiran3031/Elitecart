package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.cart.AddCartItemRequest;
import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.dto.cart.UpdateCartItemRequest;
import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.CartItem;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.CartMapper;
import com.elitecart.backend.repository.CartItemRepository;
import com.elitecart.backend.repository.CartRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Business logic for the shopping cart: add/remove/update items, clear cart,
 * and compute totals (subtotal, flat tax, flat shipping until Phase 4 wires
 * real tax/shipping rules, discount reserved for future coupon support).
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal FLAT_SHIPPING_FEE = new BigDecimal("9.99");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).orElse(null);
        int requestedQuantity = (existingItem != null ? existingItem.getQuantity() : 0) + request.getQuantity();

        validateStock(product, requestedQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(requestedQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return buildResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getOwnedItem(cart, itemId);

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return buildResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = getOwnedItem(cart, itemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return buildResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponse clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return buildResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for this user"));
    }

    private CartItem getOwnedItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() == null || product.getStockQuantity() < requestedQuantity) {
            throw new BadRequestException("Insufficient stock for '" + product.getName() + "'. Available: "
                    + (product.getStockQuantity() == null ? 0 : product.getStockQuantity()));
        }
    }

    private CartResponse buildResponse(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> cartMapper.effectivePrice(item.getProduct()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal shipping = (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO
                : FLAT_SHIPPING_FEE;
        BigDecimal discount = BigDecimal.ZERO;

        return cartMapper.toResponse(cart, tax, shipping, discount);
    }
}
