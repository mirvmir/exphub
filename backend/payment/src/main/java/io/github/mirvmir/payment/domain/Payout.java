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
public class Payout {

    private Long id;
    private Long userId;
    private Long cardId;
    private String externalPayoutId;
    private Money price;
    private PayoutStatus status;
    private String description;
    private Long walletWithdrawalId;
    private Instant createdAt;
    private Instant paidAt;

    public static Payout create(
            Long userId,
            Long cardId,
            BigDecimal amount,
            Currency currency,
            String description,
            Long walletWithdrawalId,
            Instant createdAt
    ) {
        return new Payout(
                null,
                userId,
                cardId,
                null,
                new Money(amount, currency),
                PayoutStatus.CREATED,
                description,
                walletWithdrawalId,
                createdAt,
                null
        );
    }

    public static Payout load(
            Long id,
            Long userId,
            Long cardId,
            String externalPayoutId,
            BigDecimal amount,
            Currency currency,
            PayoutStatus status,
            String description,
            Long walletWithdrawalId,
            Instant createdAt,
            Instant paidAt
    ) {
        return new Payout(
                id,
                userId,
                cardId,
                externalPayoutId,
                new Money(amount, currency),
                status,
                description,
                walletWithdrawalId,
                createdAt,
                paidAt
        );
    }

    public void markProcessing(String externalPayoutId) {
        if (PayoutStatus.CREATED != this.status) {
            throw new IllegalStateException("Payout already processed");
        }

        this.externalPayoutId = externalPayoutId;
        this.status = PayoutStatus.PROCESSING;
    }

    public void markSucceededFromWebhook(Instant paidAt) {
        if (PayoutStatus.SUCCEEDED == this.status) {
            return;
        }

        if (PayoutStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Payout cannot be succeeded");
        }

        this.status = PayoutStatus.SUCCEEDED;
        this.paidAt = paidAt;
    }

    public void markFailedFromWebhook() {
        if (PayoutStatus.FAILED == this.status) {
            return;
        }

        if (PayoutStatus.PROCESSING != this.status) {
            throw new IllegalStateException("Payout cannot be failed");
        }

        this.status = PayoutStatus.FAILED;
    }
}