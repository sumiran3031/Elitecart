package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.product.ProductImageDto;
import com.elitecart.backend.dto.product.ProductResponse;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.entity.ProductImage;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    default ProductImageDto toImageDto(ProductImage image) {
        if (image == null) {
            return null;
        }
        return ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .primary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    default ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        BigDecimal effectivePrice = (product.getDiscountPrice() != null
                && product.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0
                && product.getDiscountPrice().compareTo(product.getPrice()) < 0)
                ? product.getDiscountPrice()
                : product.getPrice();

        List<ProductImageDto> images = product.getImages().stream()
                .sorted(Comparator.comparing(
                        ProductImage::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toImageDto)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .effectivePrice(effectivePrice)
                .stockQuantity(product.getStockQuantity())
                .inStock(product.getStockQuantity() != null && product.getStockQuantity() > 0)
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .featured(product.isFeatured())
                .active(product.isActive())
                .unitsSold(product.getUnitsSold())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .images(images)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
