package io.github.mirvmir.course.application.service.port.repository;

import io.github.mirvmir.course.domain.Course;

import java.util.Set;
import java.util.UUID;

public interface CourseRepository {
    Course saveOrUpdate(Course course);
    Course findById(Long id);
    Course findActiveById(Long id);
    Course findByIdWithDraftInfo(Long id);
    Course findByIdWithDraftContent(Long id);
    Course findByIdWithDraftModules(Long id);
    Course findByIdWithDraftModuleLessons(Long id, UUID stableModuleId);
    Course findByIdWithDraftLessonBlocks(Long id, UUID stableLessonId);
    Course findByIdWithPublishedInfo(Long id);
    void updateDraftInfo(Course course);
    void updateStatus(Course course);
    void approveDraft(Course course);
    Course findByIdWithSettings(Long id);
    boolean canTeacherAccessFile(Long userId, Long fileId);
    boolean canTeacherAccessVideo(Long userId, Long videoId);
    boolean isPractice(Long courseLessonId);
    Set<Long> findCourseIdsByFileId(Long fileId);
    Set<Long> findCourseIdsByVideoId(Long videoId);
}
