package io.github.mirvmir.payment.domain;

import io.github.mirvmir.common.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Refund {

    private Long id;
    private Long paymentId;
    private String externalRefundId;
    private Money price;
    private RefundStatus status;
    private String reason;
    private Instant createdAt;
    private Instant refundedAt;

    public static Refund create(
            Long paymentId,
            BigDecimal amount,
            Currency currency,
            String reason,
            Instant now
    ) {
        return new Refund(
                null,
                paymentId,
                null,
                new Money(amount, currency),
                RefundStatus.CREATED,
                reason,
                now,
                null
        );
    }

    public static Refund load(
            Long id,
            Long paymentId,
            String externalRefundId,
            BigDecimal amount,
            Currency currency,
            RefundStatus status,
            String reason,
            Instant createdAt,
            Instant refundedAt
    ) {
        return new Refund(
                id,
                paymentId,
                externalRefundId,
                new Money(amount, currency),
                status,
                reason,
                createdAt,
                refundedAt
        );
    }

    public void markProcessing(String externalRefundId) {
        if (RefundStatus.CREATED != this.status) {
            throw new IllegalStateException("Refund already processed");
        }

        this.externalRefundId = externalRefundId;
        this.status = RefundStatus.PROCESSING;
    }

    public void markSucceededFromWebhook(Instant refundedAt) {
        if (RefundStatus.SUCCEEDED == this.status) {
            return;
        }

        if (RefundStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Refund cannot be succeeded");
        }

        this.status = RefundStatus.SUCCEEDED;
        this.refundedAt = refundedAt;
    }

    public void markFailedFromWebhook() {
        if (RefundStatus.FAILED == this.status) {
            return;
        }

        if (RefundStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Refund cannot be failed");
        }

        this.status = RefundStatus.FAILED;
    }

    public boolean isSucceeded() {
        return RefundStatus.SUCCEEDED == this.status;
    }
}