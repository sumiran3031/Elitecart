package com.elitecart.backend.service;

import com.elitecart.backend.dto.product.ProductRequest;
import com.elitecart.backend.dto.product.ProductResponse;
import com.elitecart.backend.dto.product.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    ProductResponse getById(Long id);
    ProductResponse getBySlug(String slug);
    Page<ProductResponse> search(ProductSearchCriteria criteria, Pageable pageable);
    List<ProductResponse> getFeatured();
    List<ProductResponse> getLatest();
    List<ProductResponse> getBestSellers();
}
