package io.github.mirvmir.catalog.web.response;

import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.Format;

import java.math.BigDecimal;
import java.util.Currency;

public record CatalogItemResponse(
        CatalogType type,
        Long sourceId,
        String title,
        String authorName,
        String shortDescription,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Double ratingAvg,
        Format format
) {
}