package io.github.mirvmir.payment.application.integration.dto;

public record BankPayResponse(
        String paymentId,
        String status
) {
}
