package com.elitecart.backend.service;

import com.elitecart.backend.dto.product.ProductRequest;
import com.elitecart.backend.entity.Category;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.ProductMapper;
import com.elitecart.backend.repository.CategoryRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProductServiceImpl covering the duplicate-SKU guard rail
 * and the "category must exist" validation on product creation.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest request;

    @BeforeEach
    void setUp() {
        request = ProductRequest.builder()
                .name("Wireless Mouse")
                .sku("SKU-001")
                .price(new BigDecimal("29.99"))
                .stockQuantity(50)
                .categoryId(1L)
                .build();
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenSkuAlreadyExists() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.create(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.create(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_shouldSaveProduct_whenSkuUniqueAndCategoryExists() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(Category.builder().id(1L).name("Electronics").build()));
        when(productRepository.findBySlug(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(request);

        verify(productRepository).save(any(Product.class));
    }
}
