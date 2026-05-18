package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Currency;

public record UpdateCourseDraftRequest(
        @NotBlank
        String title,
        @NotBlank
        @Size(max = 250)
        String shortDescription,
        String descriptionHtml,
        @NotNull
        @PositiveOrZero
        BigDecimal priceAmount,
        @NotNull
        Currency priceCurrency
) {
}