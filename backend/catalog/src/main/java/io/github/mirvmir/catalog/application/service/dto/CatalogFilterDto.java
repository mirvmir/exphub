package io.github.mirvmir.catalog.application.service.dto;

import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.Format;

import java.math.BigDecimal;

public record CatalogFilterDto(
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
