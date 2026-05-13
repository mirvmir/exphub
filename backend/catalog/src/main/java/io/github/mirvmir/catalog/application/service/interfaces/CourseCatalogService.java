package io.github.mirvmir.catalog.application.service.interfaces;

import io.github.mirvmir.course.api.event.CoursePublishedEvent;

import java.util.Set;

public interface CourseCatalogService {
    void addCourseToCatalog(CoursePublishedEvent event);
    void removeCourseFromCatalog(Long courseId);
    void updateTopicIds(Long courseId,
                        Set<Long> topicIds);
}
