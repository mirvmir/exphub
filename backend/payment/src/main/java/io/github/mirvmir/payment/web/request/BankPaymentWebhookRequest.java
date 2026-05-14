package io.github.mirvmir.payment.web.request;

import jakarta.validation.constraints.NotBlank;

public record BankPaymentWebhookRequest(
        @NotBlank
        String externalPaymentId,
        @NotBlank
        String status,
        String failureReason
) {
}