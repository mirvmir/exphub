package io.github.mirvmir.payment.web.request;

public record BankPaymentWebhookRequest(
        String externalPaymentId,
        String status,
        String failureReason
) {
}