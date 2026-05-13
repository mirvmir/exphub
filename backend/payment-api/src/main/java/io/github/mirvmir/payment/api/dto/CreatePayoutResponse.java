package io.github.mirvmir.payment.api.dto;

public record CreatePayoutResponse(
        Long payoutId,
        String status
) {
}