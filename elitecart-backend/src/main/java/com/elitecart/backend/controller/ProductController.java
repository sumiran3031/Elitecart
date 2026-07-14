package com.elitecart.backend.controller;

import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.dto.product.ProductResponse;
import com.elitecart.backend.dto.product.ProductSearchCriteria;
import com.elitecart.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public, read-only product endpoints for the storefront: search, filter,
 * sort, pagination, and curated listings. Product management (create/update/
 * delete) lives in AdminProductController.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product browsing, search & filter APIs")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search/filter/sort/paginate products",
            description = "All parameters are optional and combine with AND logic. " +
                    "Example sort value: ?sort=price,asc or ?sort=createdAt,desc")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Double minRating,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .featured(featured)
                .inStock(inStock)
                .minRating(minRating)
                .build();

        Page<ProductResponse> results = productService.search(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "Get a single product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @Operation(summary = "Get a single product by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getBySlug(slug)));
    }

    @Operation(summary = "Get up to 10 featured products")
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(productService.getFeatured()));
    }

    @Operation(summary = "Get up to 10 most recently added products")
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLatest() {
        return ResponseEntity.ok(ApiResponse.success(productService.getLatest()));
    }

    @Operation(summary = "Get up to 10 best-selling products")
    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getBestSellers() {
        return ResponseEntity.ok(ApiResponse.success(productService.getBestSellers()));
    }
}
