package io.github.mirvmir.payment.api.dto;

public record CreateRefundResponse(
        Long refundId,
        Long paymentId,
        String status
) {
}