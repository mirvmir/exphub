package io.github.mirvmir.payment.web.request;

public record BankPayoutWebhookRequest(
        String externalPayoutId,
        String status
) {
}