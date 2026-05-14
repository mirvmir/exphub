package io.github.mirvmir.review.web.controller;

import io.github.mirvmir.review.application.service.interfaces.ReviewService;
import io.github.mirvmir.review.web.request.CreateReviewRequest;
import io.github.mirvmir.review.web.response.ReviewResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/activities/{activityId}")
    public ReviewResponse createActivityReview(
            @PathVariable("activityId")
            Long activityId,
            @Valid
            @RequestBody
            CreateReviewRequest request
    ) {
        return reviewService.createActivityReview(activityId, request);
    }

    @PostMapping("/courses/{courseId}")
    public ReviewResponse createCourseReview(
            @PathVariable("courseId")
            Long courseId,
            @Valid
            @RequestBody
            CreateReviewRequest request
    ) {
        return reviewService.createCourseReview(courseId, request);
    }
}