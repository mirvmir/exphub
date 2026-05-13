package io.github.mirvmir.payment.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record CreateRefundRequest(
        Long orderId,
        BigDecimal amount,
        Currency currency,
        String reason
) {
}