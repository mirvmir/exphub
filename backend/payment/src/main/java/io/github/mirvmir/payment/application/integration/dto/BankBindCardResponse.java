package io.github.mirvmir.payment.application.integration.dto;

public record BankBindCardResponse(
        String bankCardId,
        String cardToken,
        String maskedPan,
        String last4,
        String paymentSystem,
        String status
) {
}
