package com.elitecart.backend.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Long id;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Builder.Default
    private boolean primary = false;

    private Integer displayOrder;
}
