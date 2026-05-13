package io.github.mirvmir.review.application.service;

import io.github.mirvmir.review.api.ReviewApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import io.github.mirvmir.review.application.service.interfaces.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DefaultReviewApi implements ReviewApi {

    private final ReviewService reviewService;

    @Override
    public ReviewRatingInfoResponse getRatingInfo(Long activityId, Long courseId) {
        return reviewService.getRatingInfo(
                activityId,
                courseId
        );
    }
}
