package io.github.mirvmir.payment.web.response;

public record ConfirmPaymentResponse(
        Long paymentId,
        String status
) {
}