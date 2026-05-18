package io.github.mirvmir.payment.application.integration.dto;

public record BankPayoutResponse(
        String externalPayoutId,
        String status
) {
}