package io.github.mirvmir.enrollment.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record OrderInfoResponse(
        Long orderId,
        BigDecimal amount,
        Currency currency
) {
}
