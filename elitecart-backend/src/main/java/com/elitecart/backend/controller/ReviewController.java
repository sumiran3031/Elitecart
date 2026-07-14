package com.elitecart.backend.controller;

import com.elitecart.backend.dto.common.ApiResponse;
import com.elitecart.backend.dto.review.ReviewRequest;
import com.elitecart.backend.dto.review.ReviewResponse;
import com.elitecart.backend.security.UserPrincipal;
import com.elitecart.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product review endpoints. Kept under /reviews (rather than nested inside
 * /products/**) so the public GET-only rule can be scoped precisely in
 * SecurityConfig without accidentally exposing the write endpoints.
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get paginated reviews for a product (public)")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getForProduct(
            @PathVariable Long productId,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsForProduct(productId, pageable)));
    }

    @Operation(summary = "Add a review for a product (one per customer per product)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable Long productId,
                                                                  @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.addReview(principal.getId(), productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review added successfully", response));
    }

    @Operation(summary = "Delete your own review")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable Long id) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(principal.getId(), id, isAdmin);
        return ResponseEntity.ok(ApiResponse.message("Review deleted successfully"));
    }
}
