package io.github.mirvmir.payment.web.request;

import jakarta.validation.constraints.NotBlank;

public record BankPayoutWebhookRequest(
        @NotBlank
        String externalPayoutId,
        @NotBlank
        String status
) {
}