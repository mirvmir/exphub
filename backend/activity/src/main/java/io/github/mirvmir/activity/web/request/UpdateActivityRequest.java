package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Currency;

public record UpdateActivityRequest(
        @NotNull
        String title,
        String shortDescription,
        String descriptionHtml,
        @NotNull
        @Positive
        Integer maxBookableSeats,
        @NotNull
        @PositiveOrZero
        BigDecimal priceAmount,
        @NotNull
        Currency priceCurrency,
        @NotNull
        @Positive
        Integer durationMinutes
) {
}
