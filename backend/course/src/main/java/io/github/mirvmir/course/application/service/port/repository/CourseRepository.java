package io.github.mirvmir.course.application.service.port.repository;

import io.github.mirvmir.course.domain.Course;

import java.util.Set;

public interface CourseRepository {
    Course saveOrUpdate(Course course);
    Course findById(Long id);
    Course findByIdWithDraftContent(Long id);
    Course findByIdWithPublishedContent(Long id);
    Course findByIdWithSettings(Long id);
    boolean canTeacherAccessFile(Long userId, Long fileId);
    boolean canTeacherAccessVideo(Long userId, Long videoId);
    boolean isPractice(Long courseLessonId);
    Set<Long> findCourseIdsByFileId(Long fileId);
    Set<Long> findCourseIdsByVideoId(Long videoId);
}
