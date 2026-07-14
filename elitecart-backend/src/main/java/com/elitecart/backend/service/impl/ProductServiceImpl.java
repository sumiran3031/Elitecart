package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.product.ProductImageDto;
import com.elitecart.backend.dto.product.ProductRequest;
import com.elitecart.backend.dto.product.ProductResponse;
import com.elitecart.backend.dto.product.ProductSearchCriteria;
import com.elitecart.backend.entity.Category;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.entity.ProductImage;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.ProductMapper;
import com.elitecart.backend.repository.CategoryRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.service.ProductService;
import com.elitecart.backend.specification.ProductSpecification;
import com.elitecart.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Product module: CRUD, image management, dynamic
 * search/filter/sort/pagination via Specifications, and curated listings
 * (featured, latest, best sellers).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("A product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String slug = resolveSlug(request.getSlug(), request.getName(), null);

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .featured(request.isFeatured())
                .active(request.isActive())
                .category(category)
                .build();

        attachImages(product, request.getImages());

        Product saved = productRepository.save(product);
        log.info("Product created: {} (sku={})", saved.getName(), saved.getSku());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("A product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        String slug = resolveSlug(request.getSlug(), request.getName(), id);

        product.setName(request.getName());
        product.setSlug(slug);
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setFeatured(request.isFeatured());
        product.setActive(request.isActive());
        product.setCategory(category);

        product.getImages().clear();
        attachImages(product, request.getImages());

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(ProductSearchCriteria criteria, Pageable pageable) {
        Page<Product> products = productRepository.findAll(
                ProductSpecification.withCriteria(criteria, true), pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeatured() {
        return productRepository.findTop10ByActiveTrueAndFeaturedTrueOrderByCreatedAtDesc().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLatest() {
        return productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBestSellers() {
        return productRepository.findTop10ByActiveTrueOrderByUnitsSoldDesc().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void attachImages(Product product, List<ProductImageDto> imageDtos) {
        if (imageDtos == null || imageDtos.isEmpty()) {
            return;
        }
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageDtos.size(); i++) {
            ProductImageDto dto = imageDtos.get(i);
            images.add(ProductImage.builder()
                    .imageUrl(dto.getImageUrl())
                    .primary(dto.isPrimary() || i == 0)
                    .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : i)
                    .product(product)
                    .build());
        }
        product.getImages().addAll(images);
    }

    private String resolveSlug(String requestedSlug, String name, Long excludeId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? SlugUtil.toSlug(requestedSlug)
                : SlugUtil.toSlug(name);

        String candidate = base;
        int suffix = 1;
        while (isSlugTaken(candidate, excludeId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean isSlugTaken(String slug, Long excludeId) {
        return productRepository.findBySlug(slug)
                .map(existing -> !existing.getId().equals(excludeId))
                .orElse(false);
    }
}
