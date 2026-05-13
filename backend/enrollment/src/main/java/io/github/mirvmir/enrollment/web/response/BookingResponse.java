package io.github.mirvmir.enrollment.web.response;

import java.math.BigDecimal;
import java.util.Currency;

public record BookingResponse(
        Long orderId,
        Long paymentId,
        String targetType,
        Long targetId,
        String title,
        BigDecimal amount,
        Currency currency,
        String orderStatus,
        String paymentStatus
) {
}
