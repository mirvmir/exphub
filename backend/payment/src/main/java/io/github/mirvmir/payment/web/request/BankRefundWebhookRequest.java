package io.github.mirvmir.payment.web.request;

public record BankRefundWebhookRequest(
        String externalRefundId,
        String status
) {
}