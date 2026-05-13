package io.github.mirvmir.payment.api.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public record PaymentSucceededEvent(
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        Currency currency,
        Instant paidAt
) {
}