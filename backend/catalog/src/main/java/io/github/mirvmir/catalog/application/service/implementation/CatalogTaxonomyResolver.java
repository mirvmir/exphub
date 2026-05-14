package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.catalog.application.service.dto.CatalogTaxonomyData;
import io.github.mirvmir.taxonomy.api.TaxonomyApi;
import io.github.mirvmir.taxonomy.api.dto.TopicTaxonomyInfoResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Component
public class CatalogTaxonomyResolver {

    private final TaxonomyApi taxonomyApi;

    public CatalogTaxonomyData resolve(Collection<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            log.debug("Taxonomy resolving skipped: topicIds is empty");

            return new CatalogTaxonomyData(
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        Set<Long> normalizedTopicIds = new HashSet<>(topicIds);

        log.debug("Resolving taxonomy for topicIds={}", normalizedTopicIds);

        Set<TopicTaxonomyInfoResponse> taxonomyInfo =
                new HashSet<>(taxonomyApi.getTopicTaxonomyInfo(normalizedTopicIds));

        Set<Long> sectionIds = taxonomyInfo.stream()
                .map(TopicTaxonomyInfoResponse::sectionId)
                .collect(Collectors.toSet());

        Set<Long> subjectIds = taxonomyInfo.stream()
                .map(TopicTaxonomyInfoResponse::subjectId)
                .collect(Collectors.toSet());

        log.debug(
                "Taxonomy resolved: topicIds={}, sectionIds={}, subjectIds={}",
                normalizedTopicIds,
                sectionIds,
                subjectIds
        );

        return new CatalogTaxonomyData(
                normalizedTopicIds,
                sectionIds,
                subjectIds
        );
    }
}
