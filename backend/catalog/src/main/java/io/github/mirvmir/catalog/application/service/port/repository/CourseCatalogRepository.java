package io.github.mirvmir.catalog.application.service.port.repository;

import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.domain.CourseCatalog;

import java.util.List;
import java.util.Set;

public interface CourseCatalogRepository {
    List<CourseCatalog> search(CatalogFilterDto filter);
    void saveOrUpdate(CourseCatalog courseCatalog);
    void deleteByCourseId(Long courseId);
    CourseCatalog findByCourseId(Long courseId);
    void updateTopicIds(Long courseId, Set<Long> topicIds);
    void updateTaxonomyIds(Long courseId,
                           Set<Long> topicIds,
                           Set<Long> sectionIds,
                           Set<Long> subjectIds);
}
