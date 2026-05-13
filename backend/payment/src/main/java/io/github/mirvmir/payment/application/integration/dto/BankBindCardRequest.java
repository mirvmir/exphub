package io.github.mirvmir.payment.application.integration.dto;

import java.math.BigDecimal;

public record BankBindCardRequest(
        String cardNumber,
        String cardHolder,
        String expiryMonth,
        String expiryYear,
        String cvc,
        BigDecimal verificationAmount,
        String idempotencyKey
) {
}
