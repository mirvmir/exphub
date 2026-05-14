package io.github.mirvmir.wallet.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;

public record WithdrawWalletRequest(
        @NotNull
        @Positive
        Long cardId,
        @NotNull
        @Positive
        BigDecimal amount,
        @NotNull
        Currency currency
) {
}