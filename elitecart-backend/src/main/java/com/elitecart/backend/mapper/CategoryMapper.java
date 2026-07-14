package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.category.CategoryResponse;
import com.elitecart.backend.entity.Category;
import org.mapstruct.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    default CategoryResponse toResponse(Category category, long productCount) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount(productCount)
                .build();
    }

    default CategoryResponse toResponseWithChildren(Category category, long productCount,
                                                      List<CategoryResponse> children) {
        CategoryResponse response = toResponse(category, productCount);
        response.setChildren(children == null ? Collections.emptyList() : children);
        return response;
    }
}
