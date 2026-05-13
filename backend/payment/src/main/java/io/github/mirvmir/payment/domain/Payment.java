package io.github.mirvmir.payment.domain;

import io.github.mirvmir.common.domain.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Payment {
    private Long id;
    private Long userId;
    private String externalPaymentId;
    private Money price;
    private PaymentStatus status;
    private String description;
    private Long orderId;
    private Instant createdAt;
    private Instant paidAt;

    public static Payment create(
            Long userId,
            BigDecimal amount,
            Currency currency,
            String description,
            Long orderId,
            Instant createdAt
    ) {
        Money price = new Money(amount, currency);
        return new Payment(
                null,
                userId,
                null,
                price,
                PaymentStatus.CREATED,
                description,
                orderId,
                createdAt,
                null
        );
    }

    public static Payment load(
            Long id,
            Long userId,
            String externalPaymentId,
            BigDecimal amount,
            Currency currency,
            PaymentStatus status,
            String description,
            Long orderId,
            Instant createdAt,
            Instant paidAt
    ) {
        Money price = new Money(amount, currency);
        return new Payment(
                id,
                userId,
                externalPaymentId,
                price,
                status,
                description,
                orderId,
                createdAt,
                paidAt
        );
    }


    public void expire() {
        this.status = PaymentStatus.EXPIRED;
    }

    public void cancel() {
        if (PaymentStatus.CANCELLED == this.status) {
            return;
        }

        if (PaymentStatus.CREATED != this.status) {
            throw new IllegalStateException("Payment cannot be cancelled");
        }

        this.status = PaymentStatus.CANCELLED;
    }

    public void markSucceeded(String externalPaymentId, Instant paidAt) {
        this.externalPaymentId = externalPaymentId;
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = paidAt;
    }

    public void markFailed(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
        this.status = PaymentStatus.FAILED;
    }

    public void markProcessing(String externalPaymentId) {
        if (PaymentStatus.CREATED != this.status) {
            throw new IllegalStateException("Payment already processed");
        }

        this.externalPaymentId = externalPaymentId;
        this.status = PaymentStatus.PROCESSING;
    }

    public void markSucceededFromWebhook(Instant paidAt) {
        if (PaymentStatus.SUCCEEDED == this.status) {
            return;
        }

        if (PaymentStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Payment cannot be succeeded");
        }

        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = paidAt;
    }

    public void markFailedFromWebhook() {
        if (PaymentStatus.FAILED == this.status) {
            return;
        }

        if (PaymentStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Payment cannot be failed");
        }

        this.status = PaymentStatus.FAILED;
    }
}