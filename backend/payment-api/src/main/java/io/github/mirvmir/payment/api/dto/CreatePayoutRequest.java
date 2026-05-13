package io.github.mirvmir.payment.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record CreatePayoutRequest(
        Long userId,
        Long cardId,
        BigDecimal amount,
        Currency currency,
        String description,
        Long walletWithdrawalId
) {
}