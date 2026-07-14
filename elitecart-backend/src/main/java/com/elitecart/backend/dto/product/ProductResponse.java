package com.elitecart.backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private Integer stockQuantity;
    private boolean inStock;
    private Double rating;
    private Integer reviewCount;
    private boolean featured;
    private boolean active;
    private Long unitsSold;
    private Long categoryId;
    private String categoryName;
    private List<ProductImageDto> images;
    private LocalDateTime createdAt;
}
