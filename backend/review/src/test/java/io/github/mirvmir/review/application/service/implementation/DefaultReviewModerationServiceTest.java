package io.github.mirvmir.review.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import io.github.mirvmir.review.application.persistence.mapper.ReviewResponseMapper;
import io.github.mirvmir.review.application.service.port.event.ReviewEventPublisher;
import io.github.mirvmir.review.application.service.port.repository.ReviewRepository;
import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewStatus;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.exception.ReviewErrorCode;
import io.github.mirvmir.review.web.response.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultReviewModerationServiceTest {

    private ReviewRepository reviewRepository;
    private ReviewResponseMapper reviewResponseMapper;
    private ReviewEventPublisher eventPublisher;

    private DefaultReviewModerationService service;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        reviewResponseMapper = mock(ReviewResponseMapper.class);
        eventPublisher = mock(ReviewEventPublisher.class);

        service = new DefaultReviewModerationService(
                reviewRepository,
                reviewResponseMapper,
                eventPublisher
        );
    }

    @Test
    void approveReview_whenActivityReview_shouldApproveAndPublishEvent() {
        Review review = activityReview(ReviewStatus.MODERATION);
        ReviewResponse expected = response(review, ReviewStatus.PUBLISHED);

        when(reviewRepository.findById(1L)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewResponseMapper.toResponse(review)).thenReturn(expected);

        ReviewResponse result = service.approveReview(1L);

        assertSame(expected, result);
        assertEquals(ReviewStatus.PUBLISHED, review.getStatus());
        verify(reviewRepository).save(review);

        ArgumentCaptor<ReviewPublishedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewPublishedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals(10L, eventCaptor.getValue().activityId());
        assertNull(eventCaptor.getValue().courseId());
        assertEquals(5.0, eventCaptor.getValue().score());
    }

    @Test
    void approveReview_whenCourseReview_shouldApproveAndPublishEvent() {
        Review review = courseReview(ReviewStatus.MODERATION);
        ReviewResponse expected = response(review, ReviewStatus.PUBLISHED);

        when(reviewRepository.findById(1L)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewResponseMapper.toResponse(review)).thenReturn(expected);

        ReviewResponse result = service.approveReview(1L);

        assertSame(expected, result);
        assertEquals(ReviewStatus.PUBLISHED, review.getStatus());
        verify(reviewRepository).save(review);

        ArgumentCaptor<ReviewPublishedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewPublishedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertNull(eventCaptor.getValue().activityId());
        assertEquals(20L, eventCaptor.getValue().courseId());
        assertEquals(4.0, eventCaptor.getValue().score());
    }

    @Test
    void approveReview_whenReviewNotFound_shouldThrowNotFoundException() {
        when(reviewRepository.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.approveReview(1L));

        assertEquals(ReviewErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
        assertEquals("Отзыв с id=1 не найден", exception.getMessage());
        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewResponseMapper, eventPublisher);
    }

    @Test
    void approveReview_whenReviewIsNotOnModeration_shouldThrowIllegalStateException() {
        Review review = activityReview(ReviewStatus.PUBLISHED);

        when(reviewRepository.findById(1L)).thenReturn(review);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.approveReview(1L));

        assertEquals("Можно подтвердить только отзыв на модерации", exception.getMessage());
        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewResponseMapper, eventPublisher);
    }

    @Test
    void rejectReview_shouldRejectReview() {
        Review review = activityReview(ReviewStatus.MODERATION);
        ReviewResponse expected = response(review, ReviewStatus.REJECTED);

        when(reviewRepository.findById(1L)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewResponseMapper.toResponse(review)).thenReturn(expected);

        ReviewResponse result = service.rejectReview(1L);

        assertSame(expected, result);
        assertEquals(ReviewStatus.REJECTED, review.getStatus());
        verify(reviewRepository).save(review);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectReview_whenReviewNotFound_shouldThrowNotFoundException() {
        when(reviewRepository.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.rejectReview(1L));

        assertEquals(ReviewErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
        assertEquals("Отзыв с id=1 не найден", exception.getMessage());
        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewResponseMapper, eventPublisher);
    }

    @Test
    void rejectReview_whenReviewIsNotOnModeration_shouldThrowIllegalStateException() {
        Review review = activityReview(ReviewStatus.REJECTED);

        when(reviewRepository.findById(1L)).thenReturn(review);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.rejectReview(1L));

        assertEquals("Можно отклонить только отзыв на модерации", exception.getMessage());
        verify(reviewRepository, never()).save(any());
        verifyNoInteractions(reviewResponseMapper, eventPublisher);
    }

    private Review activityReview(ReviewStatus status) {
        return Review.load(
                1L,
                "Хорошее занятие",
                5.0,
                10L,
                2L,
                ReviewTargetType.ACTIVITY,
                status
        );
    }

    private Review courseReview(ReviewStatus status) {
        return Review.load(
                1L,
                "Хороший курс",
                4.0,
                20L,
                3L,
                ReviewTargetType.COURSE,
                status
        );
    }

    private ReviewResponse response(Review review,
                                    ReviewStatus status) {
        return new ReviewResponse(
                review.getId(),
                review.getComment(),
                review.getScore(),
                review.getToItemId(),
                review.getFromUserId(),
                review.getTargetType(),
                status
        );
    }
}
