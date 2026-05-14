package io.github.mirvmir.activity.web.response;

import io.github.mirvmir.activity.domain.ActivityType;

import java.math.BigDecimal;
import java.util.Currency;

public record ActivityResponse(
        Long id,
        String title,
        String shortDescription,
        String descriptionHtml,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Integer durationMinutes,
        ActivityType type
) {
}
