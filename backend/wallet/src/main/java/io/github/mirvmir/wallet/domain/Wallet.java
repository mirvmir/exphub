package io.github.mirvmir.wallet.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Wallet {
    public static final BigDecimal TEACHER_ACCRUAL_RATE = new BigDecimal("0.90");

    private Long id;
    @NonNull
    private Long teacherId;
    @NonNull
    private BigDecimal balance;
    @NonNull
    private Currency currency;
    @NonNull
    private Instant createdAt;
    @NonNull
    private Instant updatedAt;

    public static Wallet create(
            Long teacherId,
            Currency currency,
            Instant now
    ) {
        return new Wallet(
                null,
                teacherId,
                BigDecimal.ZERO,
                currency,
                now,
                now
        );
    }

    public static Wallet load(
            Long id,
            Long teacherId,
            BigDecimal balance,
            Currency currency,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Wallet(
                id,
                teacherId,
                balance,
                currency,
                createdAt,
                updatedAt
        );
    }

    public BigDecimal calculateTeacherAccrualAmount(BigDecimal paymentAmount) {
        validatePositiveAmount(paymentAmount);

        return paymentAmount
                .multiply(TEACHER_ACCRUAL_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void accrue(
            BigDecimal amount,
            Currency currency,
            Instant now
    ) {
        validateCurrency(currency);
        validatePositiveAmount(amount);

        this.balance = this.balance.add(amount);
        this.updatedAt = now;
    }

    public void withdraw(
            BigDecimal amount,
            Currency currency,
            Instant now
    ) {
        validateCurrency(currency);
        validatePositiveAmount(amount);
        validateEnoughBalance(amount);

        this.balance = this.balance.subtract(amount);
        this.updatedAt = now;
    }

    private void validateCurrency(Currency currency) {
        if (!this.currency.equals(currency)) {
            throw new IllegalArgumentException("Wallet currency does not match operation currency");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void validateEnoughBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Not enough money on wallet");
        }
    }
}