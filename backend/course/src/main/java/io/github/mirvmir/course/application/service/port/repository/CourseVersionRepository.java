package io.github.mirvmir.course.application.service.port.repository;

import io.github.mirvmir.course.domain.CourseVersion;

import java.util.UUID;

public interface CourseVersionRepository {
    CourseVersion findById(Long versionId);
    CourseVersion findByIdWithModules(Long versionId);
    CourseVersion findByIdWithModule(Long versionId, UUID stableModuleId);
    CourseVersion findByIdWithLesson(Long versionId, UUID stableLessonId);
    void updateModerationState(CourseVersion version);
}