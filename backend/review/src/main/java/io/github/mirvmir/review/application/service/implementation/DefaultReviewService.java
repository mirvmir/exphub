package io.github.mirvmir.review.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import io.github.mirvmir.review.application.service.port.repository.ReviewRepository;
import io.github.mirvmir.review.application.service.interfaces.ReviewService;
import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.exception.ReviewErrorCode;
import io.github.mirvmir.review.application.persistence.mapper.ReviewResponseMapper;
import io.github.mirvmir.review.web.request.CreateReviewRequest;
import io.github.mirvmir.review.web.response.ReviewResponse;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DefaultReviewService implements ReviewService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;

    private final ReviewRepository reviewRepository;
    private final ReviewResponseMapper reviewResponseMapper;

    @Override
    @Transactional
    public ReviewResponse createActivityReview(Long activityId,
                                               CreateReviewRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        boolean isStudent = enrollmentApi.isStudentOfActivity(
                currentUserId,
                activityId
        );

        if (!isStudent) {
            throw new ForbiddenException(
                    ReviewErrorCode.USER_IS_NOT_ACTIVITY_STUDENT,
                    "Пользователь не является студентом данного занятия"
            );
        }

        Review review = checkReviewDoesNotExist(
                activityId,
                currentUserId,
                ReviewTargetType.ACTIVITY,
                request
        );

        if (review == null) {
            review = Review.create(
                    request.comment(),
                    request.score(),
                    activityId,
                    currentUserId,
                    ReviewTargetType.ACTIVITY
            );
        }

        Review savedReview = reviewRepository.save(review);

        return reviewResponseMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse createCourseReview(Long courseId,
                                             CreateReviewRequest request) {
        Long currentUserId = identityApi.getCurrentUserId();

        boolean isStudent = enrollmentApi.isStudentOfCourse(
                currentUserId,
                courseId
        );

        if (!isStudent) {
            throw new ForbiddenException(
                    ReviewErrorCode.USER_IS_NOT_COURSE_STUDENT,
                    "Пользователь не является студентом данного курса"
            );
        }

        Review review = checkReviewDoesNotExist(
                courseId,
                currentUserId,
                ReviewTargetType.COURSE,
                request
        );

        if (review == null) {
            review = Review.create(
                    request.comment(),
                    request.score(),
                    courseId,
                    currentUserId,
                    ReviewTargetType.COURSE
            );
        }

        Review savedReview = reviewRepository.save(review);

        return reviewResponseMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewRatingInfoResponse getRatingInfo(Long activityId,
                                                  Long courseId) {

        Double ratingAvg;
        Long reviewCount;
        if (courseId != null) {
            ratingAvg = reviewRepository.calculateRatingAvg(
                    courseId,
                    ReviewTargetType.COURSE
            );
            reviewCount = reviewRepository.countPublishedReviews(
                    courseId,
                    ReviewTargetType.COURSE
            );
        }
        else if (activityId != null) {
            ratingAvg = reviewRepository.calculateRatingAvg(
                    activityId,
                    ReviewTargetType.ACTIVITY
            );
            reviewCount = reviewRepository.countPublishedReviews(
                    activityId,
                    ReviewTargetType.ACTIVITY
            );
        }
        else {
            throw new IllegalStateException("не пердали для расчета рейтинга никакой тип данных");
        }

        return new ReviewRatingInfoResponse(
                ratingAvg,
                reviewCount
        );
    }

    private Review checkReviewDoesNotExist(Long toItemId,
                                           Long fromUserId,
                                           ReviewTargetType targetType,
                                           CreateReviewRequest request) {
        Review review = reviewRepository.findByTargetAndUser(
                toItemId,
                fromUserId,
                targetType
        );

        if (review != null) {
            if (review.isRejected()) {
                review.edit(
                        request.comment(),
                        request.score()
                );

                return review;
            }

            throw new BusinessException(
                    ReviewErrorCode.REVIEW_ALREADY_EXISTS
            );
        }

        return null;
    }
}