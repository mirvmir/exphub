package io.github.mirvmir.catalog.application.service.port.repository;

import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;

import java.util.List;
import java.util.Set;

public interface ActivityCatalogRepository {
    List<ActivityCatalog> search(CatalogFilterDto filter);
    void saveOrUpdate(ActivityCatalog activityCatalog);
    void deleteByActivityId(Long activityId);
    ActivityCatalog findByActivityId(Long activityId);
    void updateTopicIds(Long activityId, Set<Long> topicIds);
    void updateTaxonomyIds(Long activityId,
                           Set<Long> topicIds,
                           Set<Long> sectionIds,
                           Set<Long> subjectIds);
}
