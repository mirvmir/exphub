package io.github.mirvmir.payment.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record CreatePaymentRequest(
        Long userId,
        BigDecimal amount,
        Currency currency,
        String description,
        Long orderId
) {
}