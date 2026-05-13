package io.github.mirvmir.review.api;

import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;

public interface ReviewApi {
    ReviewRatingInfoResponse getRatingInfo(Long activityId,
                                           Long courseId);
}
