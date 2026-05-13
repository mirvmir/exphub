package io.github.mirvmir.enrollment.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CourseEnrollment {
    private Long id;
    @NonNull
    private Long courseId;
    @NonNull
    private Long publishedVersionId;
    @NonNull
    private Long userId;
    @NonNull
    private CourseEnrollmentStatus status;
    @NonNull
    private BigDecimal progressPercent;
    @NonNull
    private Instant subscribedAt;

    public static CourseEnrollment create(Instant now,
                                          Long courseId,
                                          Long publishedVersionId,
                                          Long userId) {
        return new CourseEnrollment(
                null,
                courseId,
                publishedVersionId,
                userId,
                CourseEnrollmentStatus.BOOKED,
                BigDecimal.ZERO,
                now
        );
    }

    public static CourseEnrollment load(Long id,
                                        Long courseId,
                                        Long publishedVersionId,
                                        Long userId,
                                        CourseEnrollmentStatus status,
                                        BigDecimal progressPercent,
                                        Instant subscribedAt) {
        return new CourseEnrollment(
                id,
                courseId,
                publishedVersionId,
                userId,
                status,
                progressPercent,
                subscribedAt
        );
    }

    public void confirmPayment(Instant now, long expiresMinutes) {
        if (CourseEnrollmentStatus.PAYED == this.status) {
            return;
        }

        if (CourseEnrollmentStatus.BOOKED != this.status) {
            throw new BusinessException(EnrollmentErrorCode.COURSE_NOT_BOOKED);
        }

        if (!now.isBefore(getExpiresAt(expiresMinutes))) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_EXPIRED);
        }

        this.status = CourseEnrollmentStatus.PAYED;
    }

    public void restartPayment(Instant now, long expiresMinutes) {
        if (CourseEnrollmentStatus.PAYED == this.status) {
            throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_ALREADY_PAYED);
        }

        if (now.isBefore(getExpiresAt(expiresMinutes))) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_NOT_EXPIRED);
        }

        this.status = CourseEnrollmentStatus.BOOKED;
        this.subscribedAt = now;
    }

    public void expire(Instant now, long expiresMinutes) {
        if (CourseEnrollmentStatus.BOOKED != this.status) {
            return;
        }

        if (now.isBefore(getExpiresAt(expiresMinutes))) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_NOT_EXPIRED);
        }

        this.status = CourseEnrollmentStatus.EXPIRED;
    }

    public Instant getExpiresAt(long expiresMinutes) {
        return subscribedAt.plus(expiresMinutes, ChronoUnit.MINUTES);
    }

    public void cancel() {
        if (CourseEnrollmentStatus.CANCELLED == this.status) {
            return;
        }

        if (CourseEnrollmentStatus.BOOKED == this.status
            || CourseEnrollmentStatus.PAYED == this.status) {
            this.status = CourseEnrollmentStatus.CANCELLED;
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
    }

    public void updateProgress(BigDecimal progressPercent) {
        ensureCanStudy();
        this.progressPercent = progressPercent;
    }

    public boolean isPayed() {
        return this.status == CourseEnrollmentStatus.PAYED;
    }

    public boolean isCancelled() {
        return this.status == CourseEnrollmentStatus.CANCELLED;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    private void ensureCanStudy() {
        if (CourseEnrollmentStatus.PAYED != this.status) {
            throw new BusinessException(EnrollmentErrorCode.COURSE_NOT_PAYED);
        }
    }
}