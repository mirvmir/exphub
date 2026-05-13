package io.github.mirvmir.catalog.web.request;

import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.Format;

import java.math.BigDecimal;

public record GetCatalogRequest(
        String search,
        Long topicId,
        Long sectionId,
        Long subjectId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Double minRating,
        Format format,
        CatalogType type
) {
}