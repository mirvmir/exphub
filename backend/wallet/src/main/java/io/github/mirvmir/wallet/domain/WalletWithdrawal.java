package io.github.mirvmir.wallet.domain;

import io.github.mirvmir.common.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletWithdrawal {

    private Long id;
    private Long userId;
    private Long walletId;
    private Money price;
    private WalletWithdrawalStatus status;
    private Instant createdAt;
    private Instant completedAt;

    public static WalletWithdrawal create(
            Long userId,
            Long walletId,
            BigDecimal amount,
            Currency currency,
            Instant now
    ) {
        return new WalletWithdrawal(
                null,
                userId,
                walletId,
                new Money(amount, currency),
                WalletWithdrawalStatus.CREATED,
                now,
                null
        );
    }

    public static WalletWithdrawal load(
            Long id,
            Long userId,
            Long walletId,
            BigDecimal amount,
            Currency currency,
            WalletWithdrawalStatus status,
            Instant createdAt,
            Instant completedAt
    ) {
        return new WalletWithdrawal(
                id,
                userId,
                walletId,
                new Money(amount, currency),
                status,
                createdAt,
                completedAt
        );
    }

    public void markSucceeded(Instant now) {
        this.status = WalletWithdrawalStatus.SUCCEEDED;
        this.completedAt = now;
    }

    public void markFailed() {
        this.status = WalletWithdrawalStatus.FAILED;
    }
}