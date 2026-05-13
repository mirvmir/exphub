package io.github.mirvmir.enrollment.domain.order;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

@Getter
@AllArgsConstructor
public class Order {
    private Long id;
    @NonNull
    private Long userId;
    @NonNull
    private Long enrollmentId;
    @NonNull
    private OrderTargetType targetType;
    @NonNull
    private Long targetId;
    private Long targetVersionId;
    @NonNull
    private BigDecimal amount;
    @NonNull
    private Currency currency;
    @NonNull
    private OrderStatus status;
    @NonNull
    private Instant createdAt;
    @NonNull
    private Instant expiresAt;

    public static Order createForCourse(
            Long userId,
            Long enrollmentId,
            Long courseId,
            Long publishedVersionId,
            BigDecimal amount,
            Currency currency,
            Instant now,
            long expiresMinutes
    ) {
        return new Order(
                null,
                userId,
                enrollmentId,
                OrderTargetType.COURSE,
                courseId,
                publishedVersionId,
                amount,
                currency,
                OrderStatus.CREATED,
                now,
                now.plus(expiresMinutes, ChronoUnit.MINUTES)
        );
    }

    public static Order createForActivitySlot(
            Long userId,
            Long enrollmentId,
            Long activitySlotId,
            BigDecimal amount,
            Currency currency,
            Instant now,
            long expiresMinutes
    ) {
        return new Order(
                null,
                userId,
                enrollmentId,
                OrderTargetType.ACTIVITY_SLOT,
                activitySlotId,
                null,
                amount,
                currency,
                OrderStatus.CREATED,
                now,
                now.plus(expiresMinutes, ChronoUnit.MINUTES)
        );
    }

    public static Order load(
            Long id,
            Long userId,
            Long enrollmentId,
            OrderTargetType targetType,
            Long targetId,
            Long targetVersionId,
            BigDecimal amount,
            Currency currency,
            OrderStatus status,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new Order(
                id,
                userId,
                enrollmentId,
                targetType,
                targetId,
                targetVersionId,
                amount,
                currency,
                status,
                createdAt,
                expiresAt
        );
    }

    public void markPaymentProcessing(Instant now) {
        if (OrderStatus.PAYMENT_PROCESSING == this.status) {
            return;
        }

        if (OrderStatus.CREATED != this.status) {
            throw new BusinessException(EnrollmentErrorCode.ORDER_ALREADY_PROCESSED);
        }

        if (!now.isBefore(expiresAt)) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_EXPIRED);
        }

        this.status = OrderStatus.PAYMENT_PROCESSING;
    }

    public void markPayed(Instant now) {
        if (OrderStatus.PAYED == this.status) {
            return;
        }

        if (OrderStatus.REFUND_REQUIRED == this.status
                || OrderStatus.REFUNDED == this.status) {
            return;
        }

        if (OrderStatus.CREATED != this.status
                && OrderStatus.PAYMENT_PROCESSING != this.status) {
            throw new BusinessException(EnrollmentErrorCode.ORDER_ALREADY_PROCESSED);
        }

        if (!now.isBefore(expiresAt)) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_EXPIRED);
        }

        this.status = OrderStatus.PAYED;
    }

    public void cancel() {
        if (OrderStatus.CREATED == this.status
                || OrderStatus.PAYMENT_PROCESSING == this.status) {
            this.status = OrderStatus.CANCELLED;
            return;
        }

        if (OrderStatus.PAYED == this.status) {
            this.status = OrderStatus.REFUND_REQUIRED;
            return;
        }

        if (OrderStatus.CANCELLED == this.status
                || OrderStatus.REFUND_REQUIRED == this.status
                || OrderStatus.REFUNDED == this.status) {
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ORDER_ALREADY_PROCESSED);
    }

    public void expire(Instant now) {
        if (OrderStatus.EXPIRED == this.status) {
            return;
        }

        if (OrderStatus.CREATED != this.status
            && OrderStatus.PAYMENT_PROCESSING != this.status) {
            throw new BusinessException(EnrollmentErrorCode.ORDER_ALREADY_PROCESSED);
        }

        if (now.isBefore(expiresAt)) {
            throw new BusinessException(EnrollmentErrorCode.BOOKING_NOT_EXPIRED);
        }

        this.status = OrderStatus.EXPIRED;
    }

    public void markRefundRequired() {
        if (OrderStatus.REFUND_REQUIRED == this.status) {
            return;
        }

        if (OrderStatus.CANCELLED == this.status
            || OrderStatus.EXPIRED == this.status
            || OrderStatus.PAYMENT_PROCESSING == this.status
            || OrderStatus.PAYED == this.status) {
            this.status = OrderStatus.REFUND_REQUIRED;
            return;
        }

        throw new BusinessException(EnrollmentErrorCode.ORDER_CANNOT_BE_REFUNDED);
    }

    public void markRefunded() {
        if (OrderStatus.REFUNDED == this.status) {
            return;
        }

        if (OrderStatus.REFUND_REQUIRED != this.status) {
            throw new BusinessException(EnrollmentErrorCode.ORDER_CANNOT_BE_REFUNDED);
        }

        this.status = OrderStatus.REFUNDED;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isExpiredStatus() {
        return OrderStatus.EXPIRED == this.status;
    }

    public boolean isRefundRequired() {
        return OrderStatus.REFUND_REQUIRED == this.status;
    }

    public boolean isCancelled() {
        return OrderStatus.CANCELLED == this.status;
    }

    public boolean isPaymentProcessing() {
        return OrderStatus.PAYMENT_PROCESSING == this.status;
    }

    public boolean isPayed() {
        return OrderStatus.PAYED == this.status;
    }

    public boolean isRefunded() {
        return OrderStatus.REFUNDED == this.status;
    }

    public boolean isFinalStatus() {
        return OrderStatus.PAYED == this.status
                || OrderStatus.CANCELLED == this.status
                || OrderStatus.REFUNDED == this.status
                || OrderStatus.EXPIRED == this.status;
    }
}
