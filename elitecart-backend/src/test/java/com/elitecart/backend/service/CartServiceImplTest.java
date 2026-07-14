package com.elitecart.backend.service;

import com.elitecart.backend.dto.cart.AddCartItemRequest;
import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.mapper.CartMapper;
import com.elitecart.backend.repository.CartItemRepository;
import com.elitecart.backend.repository.CartRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit test for CartServiceImpl covering the insufficient-stock guard rail
 * when adding an item to the cart.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addItem_shouldThrowBadRequestException_whenRequestedQuantityExceedsStock() {
        Long userId = 1L;
        Cart cart = Cart.builder().id(10L).build();
        Product product = Product.builder().id(5L).name("Limited Edition Sneakers")
                .price(new BigDecimal("199.99")).stockQuantity(2).build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(10L, 5L)).thenReturn(Optional.empty());

        AddCartItemRequest request = AddCartItemRequest.builder().productId(5L).quantity(5).build();

        assertThrows(BadRequestException.class, () -> cartService.addItem(userId, request));
    }
}
