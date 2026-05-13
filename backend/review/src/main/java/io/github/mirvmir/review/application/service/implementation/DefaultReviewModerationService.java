package io.github.mirvmir.review.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import io.github.mirvmir.review.application.service.port.event.ReviewEventPublisher;
import io.github.mirvmir.review.application.service.port.repository.ReviewRepository;
import io.github.mirvmir.review.application.service.interfaces.ReviewModerationService;
import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.exception.ReviewErrorCode;
import io.github.mirvmir.review.application.persistence.mapper.ReviewResponseMapper;
import io.github.mirvmir.review.web.response.ReviewResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DefaultReviewModerationService implements ReviewModerationService {

    private final ReviewRepository reviewRepository;
    private final ReviewResponseMapper reviewResponseMapper;
    private final ReviewEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse approveReview(Long reviewId) {
        Review review = getExistingReview(reviewId);

        review.approve();

        Review savedReview = reviewRepository.save(review);

        if (ReviewTargetType.ACTIVITY == review.getTargetType()) {
            eventPublisher.publish(new ReviewPublishedEvent(
                    review.getToItemId(),
                    null,
                    review.getScore()
            ));
        }
        if (ReviewTargetType.COURSE == review.getTargetType()) {
            eventPublisher.publish(new ReviewPublishedEvent(
                    null,
                    review.getToItemId(),
                    review.getScore()
            ));
        }

        return reviewResponseMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse rejectReview(Long reviewId) {
        Review review = getExistingReview(reviewId);

        review.reject();

        Review savedReview = reviewRepository.save(review);

        return reviewResponseMapper.toResponse(savedReview);
    }

    private Review getExistingReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId);

        if (review == null) {
            throw new NotFoundException(
                    ReviewErrorCode.REVIEW_NOT_FOUND,
                    "Отзыв с id=" + reviewId + " не найден"
            );
        }

        return review;
    }
}