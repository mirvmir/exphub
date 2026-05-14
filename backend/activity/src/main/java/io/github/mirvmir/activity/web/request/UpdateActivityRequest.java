package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;

public record UpdateActivityRequest(
        String title,
        String shortDescription,
        String descriptionHtml,
        @Positive
        Integer maxBookableSeats,
        @Positive
        BigDecimal priceAmount,
        Currency priceCurrency,
        @Positive
        Integer durationMinutes
) {
}
