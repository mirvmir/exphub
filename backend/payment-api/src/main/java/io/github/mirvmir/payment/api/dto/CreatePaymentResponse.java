package io.github.mirvmir.payment.api.dto;

public record CreatePaymentResponse(
        Long paymentId,
        String status
) {
}