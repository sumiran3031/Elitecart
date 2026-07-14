package com.elitecart.backend.service;

import com.elitecart.backend.dto.review.ReviewRequest;
import com.elitecart.backend.exception.DuplicateResourceException;
import com.elitecart.backend.mapper.ReviewMapper;
import com.elitecart.backend.repository.ProductRepository;
import com.elitecart.backend.repository.ReviewRepository;
import com.elitecart.backend.repository.UserRepository;
import com.elitecart.backend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for ReviewServiceImpl covering the one-review-per-customer
 * guard rail.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void addReview_shouldThrowDuplicateResourceException_whenUserAlreadyReviewedProduct() {
        Long userId = 1L;
        Long productId = 10L;
        when(reviewRepository.existsByProductIdAndUserId(productId, userId)).thenReturn(true);

        ReviewRequest request = ReviewRequest.builder().rating(5).comment("Great!").build();

        assertThrows(DuplicateResourceException.class, () -> reviewService.addReview(userId, productId, request));

        verify(productRepository, never()).findById(productId);
        verify(reviewRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
