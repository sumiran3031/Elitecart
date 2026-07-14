package com.elitecart.backend.service;

import com.elitecart.backend.dto.review.ReviewRequest;
import com.elitecart.backend.dto.review.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse addReview(Long userId, Long productId, ReviewRequest request);
    Page<ReviewResponse> getReviewsForProduct(Long productId, Pageable pageable);
    void deleteReview(Long userId, Long reviewId, boolean isAdmin);
}
