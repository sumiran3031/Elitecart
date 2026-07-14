package com.elitecart.backend.mapper;

import com.elitecart.backend.dto.review.ReviewResponse;
import com.elitecart.backend.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    default ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        String userName = review.getUser().getFirstName() + " " + review.getUser().getLastName();
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(userName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
