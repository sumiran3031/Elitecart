package com.elitecart.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 150, message = "Category name must not exceed 150 characters")
    private String name;

    /** Optional — auto-generated from name if left blank. */
    private String slug;

    private String description;

    private String imageUrl;

    /** Null for a top-level category. */
    private Long parentId;

    @Builder.Default
    private boolean active = true;
}
