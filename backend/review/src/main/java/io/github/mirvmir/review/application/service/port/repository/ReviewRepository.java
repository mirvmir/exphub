package io.github.mirvmir.review.application.service.port.repository;

import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewTargetType;

public interface ReviewRepository {
    Review save(Review review);
    Review findById(Long id);
    Review findByTargetAndUser(Long toItemId,
                               Long fromUserId,
                               ReviewTargetType targetType);
    Long countPublishedReviews(Long toItemId,
                               ReviewTargetType targetType);
    Double calculateRatingAvg(Long toItemId,
                              ReviewTargetType targetType);
}