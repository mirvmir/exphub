package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Currency;

public record UpdateCourseDraftRequest(
        String title,
        String shortDescription,
        String descriptionHtml,
        @Positive
        BigDecimal priceAmount,
        Currency priceCurrency
) {
}