package io.github.mirvmir.payment.application.integration.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record BankPayRequest(
        String cardToken,
        BigDecimal amount,
        Currency currency,
        Long orderId,
        String description,
        String callbackUrl
) {
}