package com.elitecart.backend.repository;

import com.elitecart.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    long countByCategoryId(Long categoryId);
    java.util.List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();
    java.util.List<Product> findTop10ByActiveTrueOrderByUnitsSoldDesc();
    java.util.List<Product> findTop10ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc();
}
