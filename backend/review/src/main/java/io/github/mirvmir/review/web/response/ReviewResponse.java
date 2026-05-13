package io.github.mirvmir.review.web.response;

import io.github.mirvmir.review.domain.ReviewStatus;
import io.github.mirvmir.review.domain.ReviewTargetType;

public record ReviewResponse(
        Long id,
        String comment,
        Double score,
        Long toItemId,
        Long fromUserId,
        ReviewTargetType targetType,
        ReviewStatus status
) {
}