package io.github.mirvmir.catalog.application.service.dto;

import java.util.Set;

public record CatalogTaxonomyData(
        Set<Long> topicIds,
        Set<Long> sectionIds,
        Set<Long> subjectIds
) {
}