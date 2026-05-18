package io.github.mirvmir.activity.web.request;

import io.github.mirvmir.activity.domain.ActivityType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record CreateActivityRequest(
        @NotBlank
        String title,
        @NotBlank
        @Size(max = 250)
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
        Integer durationMinutes,
        @NotNull
        ActivityType type,
        @Positive
        Integer bookingStepMinutes
) {
}
