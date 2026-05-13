package io.github.mirvmir.review.web.controller;

import io.github.mirvmir.review.application.service.interfaces.ReviewModerationService;
import io.github.mirvmir.review.web.response.ReviewResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class ReviewModerationController {

    private final ReviewModerationService reviewModerationService;

    @PatchMapping("/{reviewId}/approve")
    public ReviewResponse approveReview(
            @PathVariable("reviewId")
            Long reviewId
    ) {
        return reviewModerationService.approveReview(reviewId);
    }

    @PatchMapping("/{reviewId}/reject")
    public ReviewResponse rejectReview(
            @PathVariable("reviewId")
            Long reviewId
    ) {
        return reviewModerationService.rejectReview(reviewId);
    }
}