package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.wishlist.WishlistItemResponse;
import com.elitecart.backend.dto.wishlist.WishlistResponse;
import com.elitecart.backend.entity.Wishlist;
import com.elitecart.backend.entity.WishlistItem;
import com.elitecart.backend.entity.Product;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    default BigDecimal effectivePrice(Product product) {
        if (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                && product.getDiscountPrice().compareTo(product.getPrice()) < 0) {
            return product.getDiscountPrice();
        }
        return product.getPrice();
    }

    default WishlistItemResponse toItemResponse(WishlistItem item) {
        Product product = item.getProduct();
        String imageUrl = product.getImages().stream()
                .min(Comparator.comparing(img -> img.isPrimary() ? 0 : 1))
                .map(img -> img.getImageUrl())
                .orElse(null);

        return WishlistItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .price(product.getPrice())
                .effectivePrice(effectivePrice(product))
                .inStock(product.getStockQuantity() != null && product.getStockQuantity() > 0)
                .build();
    }

    default WishlistResponse toResponse(Wishlist wishlist) {
        List<WishlistItemResponse> items = wishlist.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .id(wishlist.getId())
                .items(items)
                .build();
    }
}
