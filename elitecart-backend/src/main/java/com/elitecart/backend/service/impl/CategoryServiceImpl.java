package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.category.CategoryRequest;
import com.elitecart.backend.dto.category.CategoryResponse;
import com.elitecart.backend.entity.Category;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.CategoryMapper;
import com.elitecart.backend.repository.CategoryRepository;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.service.CategoryService;
import com.elitecart.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the Category module: CRUD, nested (parent/child)
 * hierarchy, slug generation/uniqueness, and the flat vs tree listing views.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getName(), null);

        Category parent = resolveParent(request.getParentId());

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .parent(parent)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} (slug={})", saved.getName(), saved.getSlug());
        return categoryMapper.toResponse(saved, 0);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new BadRequestException("A category cannot be its own parent");
        }

        String slug = resolveSlug(request.getSlug(), request.getName(), id);
        Category parent = resolveParent(request.getParentId());

        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.isActive());
        category.setParent(parent);

        Category saved = categoryRepository.save(category);
        long productCount = productRepository.countByCategoryId(saved.getId());
        return categoryMapper.toResponse(saved, productCount);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (productRepository.countByCategoryId(id) > 0) {
            throw new BadRequestException("Cannot delete a category that still has products assigned to it");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        long productCount = productRepository.countByCategoryId(id);
        return categoryMapper.toResponse(category, productCount);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        long productCount = productRepository.countByCategoryId(category.getId());
        return categoryMapper.toResponse(category, productCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllFlat() {
        return categoryRepository.findAll().stream()
                .map(category -> categoryMapper.toResponse(category, productRepository.countByCategoryId(category.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getTree() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        return roots.stream()
                .sorted(Comparator.comparing(Category::getName))
                .map(this::buildTreeNode)
                .collect(Collectors.toList());
    }

    private CategoryResponse buildTreeNode(Category category) {
        long productCount = productRepository.countByCategoryId(category.getId());
        List<CategoryResponse> children = category.getChildren().stream()
                .sorted(Comparator.comparing(Category::getName))
                .map(this::buildTreeNode)
                .collect(Collectors.toList());
        return categoryMapper.toResponseWithChildren(category, productCount, children);
    }

    private Category resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + parentId));
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
        return categoryRepository.findBySlug(slug)
                .map(existing -> !existing.getId().equals(excludeId))
                .orElse(false);
    }
}
