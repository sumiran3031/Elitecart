package com.elitecart.backend.service.impl;

import com.elitecart.backend.dto.review.ReviewRequest;
import com.elitecart.backend.dto.review.ReviewResponse;
import com.elitecart.backend.entity.Product;
import com.elitecart.backend.entity.Review;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.exception.BadRequestException;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.exception.ResourceNotFoundException;
import com.elitecart.backend.mapper.ReviewMapper;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.repository.ReviewRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for product reviews: one review per customer per product,
 * and every add/delete recomputes the product's aggregate rating + review
 * count (used by the storefront and the Phase 2 product listing/filtering).
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse addReview(Long userId, Long productId, ReviewRequest request) {
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        recomputeProductRating(product);

        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own review");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        recomputeProductRating(product);
    }

    private void recomputeProductRating(Product product) {
        Double average = reviewRepository.averageRatingForProduct(product.getId());
        long count = reviewRepository.countForProduct(product.getId());
        product.setRating(average != null ? Math.round(average * 10) / 10.0 : 0.0);
        product.setReviewCount((int) count);
        productRepository.save(product);
    }
}
