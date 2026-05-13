package io.github.mirvmir.course.web.request;

import java.math.BigDecimal;
import java.util.Currency;

public record UpdateCourseDraftRequest(
        String title,
        String shortDescription,
        String descriptionHtml,
        BigDecimal priceAmount,
        Currency priceCurrency
) {
}