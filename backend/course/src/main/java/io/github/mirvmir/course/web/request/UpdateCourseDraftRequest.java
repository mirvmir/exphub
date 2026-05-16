package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
        @Positive
        BigDecimal priceAmount,
        @NotNull
        Currency priceCurrency
) {
}