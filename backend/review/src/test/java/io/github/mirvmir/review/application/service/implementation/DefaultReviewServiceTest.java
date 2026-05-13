package io.github.mirvmir.review.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import io.github.mirvmir.review.application.persistence.mapper.ReviewResponseMapper;
import io.github.mirvmir.review.application.service.port.repository.ReviewRepository;
import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewStatus;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.exception.ReviewErrorCode;
import io.github.mirvmir.review.web.request.CreateReviewRequest;
import io.github.mirvmir.review.web.response.ReviewResponse;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultReviewServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private ReviewRepository reviewRepository;
    private ReviewResponseMapper reviewResponseMapper;

    private DefaultReviewService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        reviewRepository = mock(ReviewRepository.class);
        reviewResponseMapper = mock(ReviewResponseMapper.class);

        service = new DefaultReviewService(
                identityApi,
                enrollmentApi,
                reviewRepository,
                reviewResponseMapper
        );
    }

    @Test
    void createActivityReview_shouldCreateReview() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );
        Review savedReview = activityReview(1L, ReviewStatus.MODERATION);
        ReviewResponse expected = response(savedReview);

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);
        when(reviewRepository.findByTargetAndUser(10L, 2L, ReviewTargetType.ACTIVITY))
                .thenReturn(null);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewResponseMapper.toResponse(savedReview)).thenReturn(expected);

        ReviewResponse result = service.createActivityReview(10L, request);

        assertSame(expected, result);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals("Хорошее занятие", reviewCaptor.getValue().getComment());
        assertEquals(5.0, reviewCaptor.getValue().getScore());
        assertEquals(10L, reviewCaptor.getValue().getToItemId());
        assertEquals(2L, reviewCaptor.getValue().getFromUserId());
        assertEquals(ReviewTargetType.ACTIVITY, reviewCaptor.getValue().getTargetType());
        assertEquals(ReviewStatus.MODERATION, reviewCaptor.getValue().getStatus());
    }

    @Test
    void createActivityReview_whenCurrentUserIsNotStudent_shouldThrowForbidden() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.createActivityReview(10L, request));

        assertEquals(ReviewErrorCode.USER_IS_NOT_ACTIVITY_STUDENT, exception.getErrorCode());
        verifyNoInteractions(reviewRepository, reviewResponseMapper);
    }

    @Test
    void createCourseReview_shouldCreateReview() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хороший курс",
                4.0
        );
        Review savedReview = courseReview(1L, ReviewStatus.MODERATION);
        ReviewResponse expected = response(savedReview);

        when(identityApi.getCurrentUserId()).thenReturn(3L);
        when(enrollmentApi.isStudentOfCourse(3L, 20L)).thenReturn(true);
        when(reviewRepository.findByTargetAndUser(20L, 3L, ReviewTargetType.COURSE))
                .thenReturn(null);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewResponseMapper.toResponse(savedReview)).thenReturn(expected);

        ReviewResponse result = service.createCourseReview(20L, request);

        assertSame(expected, result);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals("Хороший курс", reviewCaptor.getValue().getComment());
        assertEquals(4.0, reviewCaptor.getValue().getScore());
        assertEquals(20L, reviewCaptor.getValue().getToItemId());
        assertEquals(3L, reviewCaptor.getValue().getFromUserId());
        assertEquals(ReviewTargetType.COURSE, reviewCaptor.getValue().getTargetType());
        assertEquals(ReviewStatus.MODERATION, reviewCaptor.getValue().getStatus());
    }

    @Test
    void createCourseReview_whenCurrentUserIsNotStudent_shouldThrowForbidden() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хороший курс",
                4.0
        );

        when(identityApi.getCurrentUserId()).thenReturn(3L);
        when(enrollmentApi.isStudentOfCourse(3L, 20L)).thenReturn(false);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.createCourseReview(20L, request));

        assertEquals(ReviewErrorCode.USER_IS_NOT_COURSE_STUDENT, exception.getErrorCode());
        verifyNoInteractions(reviewRepository, reviewResponseMapper);
    }

    @Test
    void createReview_whenScoreIsNull_shouldThrowValidationException() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Без оценки",
                null
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createActivityReview(10L, request));

        assertEquals("Оценка обязательна", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_whenScoreIsLessThanOne_shouldThrowValidationException() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Плохая оценка",
                0.0
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createActivityReview(10L, request));

        assertEquals("Оценка должна быть от 1 до 5", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_whenScoreIsGreaterThanFive_shouldThrowValidationException() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Плохая оценка",
                6.0
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createActivityReview(10L, request));

        assertEquals("Оценка должна быть от 1 до 5", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_whenReviewAlreadyExists_shouldThrowBusinessException() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Повторный отзыв",
                5.0
        );
        Review existingReview = activityReview(1L, ReviewStatus.MODERATION);

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);
        when(reviewRepository.findByTargetAndUser(10L, 2L, ReviewTargetType.ACTIVITY))
                .thenReturn(existingReview);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createActivityReview(10L, request));

        assertEquals(ReviewErrorCode.REVIEW_ALREADY_EXISTS, exception.getErrorCode());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_whenRejectedReviewExists_shouldEditExistingReview() {
        CreateReviewRequest request = new CreateReviewRequest(
                "Исправленный отзыв",
                4.0
        );

        Review existingReview = activityReview(1L, ReviewStatus.REJECTED);
        ReviewResponse expected = response(existingReview);

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(enrollmentApi.isStudentOfActivity(2L, 10L)).thenReturn(true);
        when(reviewRepository.findByTargetAndUser(10L, 2L, ReviewTargetType.ACTIVITY))
                .thenReturn(existingReview);
        when(reviewRepository.save(existingReview)).thenReturn(existingReview);
        when(reviewResponseMapper.toResponse(existingReview)).thenReturn(expected);

        ReviewResponse result = service.createActivityReview(10L, request);

        assertSame(expected, result);
        assertEquals("Исправленный отзыв", existingReview.getComment());
        assertEquals(4.0, existingReview.getScore(), 0.001);
        assertEquals(ReviewStatus.MODERATION, existingReview.getStatus());

        verify(reviewRepository).save(existingReview);
        verify(reviewRepository, never()).save(argThat(review -> review != existingReview));
    }

    @Test
    void getRatingInfo_whenCourseIdPassed_shouldReturnCourseRatingInfo() {
        when(reviewRepository.calculateRatingAvg(20L, ReviewTargetType.COURSE))
                .thenReturn(4.5);
        when(reviewRepository.countPublishedReviews(20L, ReviewTargetType.COURSE))
                .thenReturn(12L);

        ReviewRatingInfoResponse result = service.getRatingInfo(null, 20L);

        assertEquals(4.5, result.ratingAvg());
        assertEquals(12L, result.reviewCount());
        verify(reviewRepository).calculateRatingAvg(20L, ReviewTargetType.COURSE);
        verify(reviewRepository).countPublishedReviews(20L, ReviewTargetType.COURSE);
        verify(reviewRepository, never()).calculateRatingAvg(anyLong(), eq(ReviewTargetType.ACTIVITY));
    }

    @Test
    void getRatingInfo_whenActivityIdPassed_shouldReturnActivityRatingInfo() {
        when(reviewRepository.calculateRatingAvg(10L, ReviewTargetType.ACTIVITY))
                .thenReturn(4.0);
        when(reviewRepository.countPublishedReviews(10L, ReviewTargetType.ACTIVITY))
                .thenReturn(3L);

        ReviewRatingInfoResponse result = service.getRatingInfo(10L, null);

        assertEquals(4.0, result.ratingAvg());
        assertEquals(3L, result.reviewCount());
        verify(reviewRepository).calculateRatingAvg(10L, ReviewTargetType.ACTIVITY);
        verify(reviewRepository).countPublishedReviews(10L, ReviewTargetType.ACTIVITY);
    }

    @Test
    void getRatingInfo_whenIdsAreNull_shouldThrowIllegalStateException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.getRatingInfo(null, null));

        assertEquals("не пердали для расчета рейтинга никакой тип данных", exception.getMessage());
        verifyNoInteractions(reviewRepository);
    }

    private Review activityReview(Long id,
                                  ReviewStatus status) {
        return Review.load(
                id,
                "Хорошее занятие",
                5.0,
                10L,
                2L,
                ReviewTargetType.ACTIVITY,
                status
        );
    }

    private Review courseReview(Long id,
                                ReviewStatus status) {
        return Review.load(
                id,
                "Хороший курс",
                4.0,
                20L,
                3L,
                ReviewTargetType.COURSE,
                status
        );
    }

    private ReviewResponse response(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getComment(),
                review.getScore(),
                review.getToItemId(),
                review.getFromUserId(),
                review.getTargetType(),
                review.getStatus()
        );
    }
}
