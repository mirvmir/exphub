package io.github.mirvmir.course.application.service.port.repository;

import io.github.mirvmir.course.domain.CourseVersion;

import java.util.UUID;

public interface CourseVersionRepository {
    CourseVersion findByIdAndCourseId(
            Long versionId,
            Long courseId);
    CourseVersion findByIdAndCourseIdWithModules(Long versionId,
                                                 Long courseId);

    CourseVersion findByIdAndCourseIdWithModule(Long versionId,
                                                Long courseId,
                                                UUID stableModuleId);
    CourseVersion findByIdAndCourseIdWithLesson(Long versionId,
                                                Long courseId,
                                                UUID stableLessonId);
}
