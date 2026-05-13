package io.github.mirvmir.wallet.domain;

import io.github.mirvmir.common.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WalletTransaction {
    private Long id;
    private Long walletId;
    private Long paymentId;
    private Long walletWithdrawalId;
    private Money price;
    private WalletTransactionType type;
    private Instant createdAt;

    public static WalletTransaction createWithdrawal(Long walletId,
                                                     Long walletWithdrawalId,
                                                     BigDecimal amount,
                                                     Currency currency,
                                                     WalletTransactionType type,
                                                     Instant now) {
        return new WalletTransaction(
                null,
                walletId,
                null,
                walletWithdrawalId,
                new Money(amount, currency),
                type,
                now
        );
    }

    public static WalletTransaction createPayment(Long walletId,
                                                  Long paymentId,
                                                  BigDecimal amount,
                                                  Currency currency,
                                                  WalletTransactionType type,
                                                  Instant now) {
        return new WalletTransaction(
                null,
                walletId,
                paymentId,
                null,
                new Money(amount, currency),
                type,
                now
        );
    }

    public static WalletTransaction load(Long id,
                                         Long walletId,
                                         Long paymentId,
                                         Long walletWithdrawalId,
                                         Money price,
                                         WalletTransactionType type,
                                         Instant createdAt) {
        return new WalletTransaction(
                id,
                walletId,
                paymentId,
                walletWithdrawalId,
                price,
                type,
                createdAt
        );
    }
}