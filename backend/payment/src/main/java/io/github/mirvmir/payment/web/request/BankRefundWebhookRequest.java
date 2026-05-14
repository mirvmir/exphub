package io.github.mirvmir.payment.web.request;

import jakarta.validation.constraints.NotBlank;

public record BankRefundWebhookRequest(
        @NotBlank
        String externalRefundId,
        @NotBlank
        String status
) {
}