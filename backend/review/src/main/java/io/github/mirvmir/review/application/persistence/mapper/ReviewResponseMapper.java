package io.github.mirvmir.review.application.persistence.mapper;

import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.web.response.ReviewResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewResponseMapper {
    ReviewResponse toResponse(Review review);
}