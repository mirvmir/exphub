package io.github.mirvmir.review.application.service.interfaces;

import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import io.github.mirvmir.review.web.request.CreateReviewRequest;
import io.github.mirvmir.review.web.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createActivityReview(Long activityId,
                                        CreateReviewRequest request);
    ReviewResponse createCourseReview(Long courseId,
                                      CreateReviewRequest request);
    ReviewRatingInfoResponse getRatingInfo(Long activityId,
                                           Long courseId);
}
