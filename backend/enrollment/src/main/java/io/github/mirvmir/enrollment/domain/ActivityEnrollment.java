package io.github.mirvmir.enrollment.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class ActivityEnrollment {
    private Long id;
    @NonNull
    private Long activitySlotId;
    @NonNull
    private Long userId;
    @NonNull
    private ActivityEnrollmentStatus status;
    @NonNull
    private final Instant subscribedAt;

    public static ActivityEnrollment create(Instant now,
                                            Long activitySlotId,
                                            Long userId) {
        return new ActivityEnrollment(
                null,
                activitySlotId,
                userId,
                ActivityEnrollmentStatus.BOOKED,
                now
        );
    }

    public static ActivityEnrollment load(Long id,
                                          Long activitySlotId,
                                          Long userId,
                                          ActivityEnrollmentStatus status,
                                          Instant subscribedAt) {
        return new ActivityEnrollment(
                id,
                activitySlotId,
                userId,
                status,
                subscribedAt
        );
    }

    public void confirmPayment(Instant now, long expiresMinutes) {
        if (ActivityEnrollmentStatus.PAYED == this.status) {
            return;
        }

        ensureStatus(ActivityEnrollmentStatus.BOOKED);

        if (!now.isBefore(getExpiresAt(expiresMinutes))) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_EXPIRED);
        }

        this.status = ActivityEnrollmentStatus.PAYED;
    }
    public void expire(Instant now, long expiresMinutes) {
        if (ActivityEnrollmentStatus.EXPIRED == this.status) {
            return;
        }

        if (ActivityEnrollmentStatus.BOOKED != this.status) {
            return;
        }

        if (now.isBefore(getExpiresAt(expiresMinutes))) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_NOT_EXPIRED);
        }

        this.status = ActivityEnrollmentStatus.EXPIRED;
    }

    public Instant getExpiresAt(long expiresMinutes) {
        return subscribedAt.plus(expiresMinutes, ChronoUnit.MINUTES);
    }

    public void cancel() {
        if (isCancelled()) {
            return;
        }

        if (ActivityEnrollmentStatus.PAYED == this.status
            || ActivityEnrollmentStatus.BOOKED == this.status) {
            this.status = ActivityEnrollmentStatus.CANCELLED_TEACHER;
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
    }

    public void cancelByStudent() {
        if (ActivityEnrollmentStatus.PAYED == this.status) {
            this.status = ActivityEnrollmentStatus.CANCELLED_STUDENT;
            return;
        }

        if (ActivityEnrollmentStatus.BOOKED == this.status) {
            this.status = ActivityEnrollmentStatus.CANCELLED;
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
    }

    public void cancelByTeacher() {
        if (ActivityEnrollmentStatus.PAYED == this.status
                || ActivityEnrollmentStatus.BOOKED == this.status) {
            this.status = ActivityEnrollmentStatus.CANCELLED_TEACHER;
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_CANNOT_BE_CANCELLED);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public boolean isPayed() {
        return this.status == ActivityEnrollmentStatus.PAYED;
    }

    public boolean isCancelled() {
        return this.status == ActivityEnrollmentStatus.CANCELLED
                || this.status == ActivityEnrollmentStatus.CANCELLED_STUDENT
                || this.status == ActivityEnrollmentStatus.CANCELLED_TEACHER;
    }

    private void ensureStatus(ActivityEnrollmentStatus expectedStatus) {
        if (this.status != expectedStatus) {
            switch (expectedStatus) {
                case BOOKED -> throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_NOT_BOOKED);
                case PAYED -> throw new BusinessException(EnrollmentErrorCode.ENROLLMENT_NOT_PAYED);
                default -> throw new IllegalStateException("Неподдерживаемая проверка статуса");
            }
        }
    }
}