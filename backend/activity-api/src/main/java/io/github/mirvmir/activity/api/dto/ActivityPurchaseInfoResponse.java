package io.github.mirvmir.activity.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record ActivityPurchaseInfoResponse(
        Long activityId,
        Long authorId,
        String title,
        BigDecimal priceAmount,
        Currency priceCurrency,
        boolean active
) {
}
