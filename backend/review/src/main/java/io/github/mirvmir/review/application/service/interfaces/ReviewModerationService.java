package io.github.mirvmir.review.application.service.interfaces;

import io.github.mirvmir.review.web.response.ReviewResponse;

public interface ReviewModerationService {
    ReviewResponse approveReview(Long reviewId);
    ReviewResponse rejectReview(Long reviewId);
}