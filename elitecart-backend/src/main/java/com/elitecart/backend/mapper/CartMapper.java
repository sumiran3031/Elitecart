package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.cart.CartItemResponse;
import com.elitecart.backend.dto.cart.CartResponse;
import com.elitecart.backend.entity.Cart;
import com.elitecart.backend.entity.CartItem;
import com.elitecart.backend.entity.Product;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CartMapper {

    default BigDecimal effectivePrice(Product product) {
        if (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                && product.getDiscountPrice().compareTo(product.getPrice()) < 0) {
            return product.getDiscountPrice();
        }
        return product.getPrice();
    }

    default CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal unitPrice = effectivePrice(product);
        String imageUrl = product.getImages().stream()
                .min(Comparator.comparing(img -> img.isPrimary() ? 0 : 1))
                .map(img -> img.getImageUrl())
                .orElse(null);

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
                .availableStock(product.getStockQuantity())
                .build();
    }

    default CartResponse toResponse(Cart cart, BigDecimal tax, BigDecimal shipping, BigDecimal discount) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = subtotal.add(tax).add(shipping).subtract(discount);
        int totalItems = items.stream().mapToInt(CartItemResponse::getQuantity).sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .tax(tax)
                .shipping(shipping)
                .discount(discount)
                .grandTotal(grandTotal.max(BigDecimal.ZERO))
                .totalItems(totalItems)
                .build();
    }
}
