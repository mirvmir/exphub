package io.github.mirvmir.enrollment.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public record OrderPaymentInfoResponse(
        Long orderId,
        Long userId,
        Long enrollmentId,
        String targetType,
        Long targetId,
        Long targetVersionId,
        BigDecimal amount,
        Currency currency,
        Instant expiresAt,
        boolean expired,
        boolean paymentProcessing,
        boolean payed,
        boolean finalStatus
) {
}
