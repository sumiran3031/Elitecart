package com.elitecart.backend.service;

import com.elitecart.backend.dto.category.CategoryRequest;
import com.elitecart.backend.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
    CategoryResponse getById(Long id);
    CategoryResponse getBySlug(String slug);
    List<CategoryResponse> getAllFlat();
    List<CategoryResponse> getTree();
}
