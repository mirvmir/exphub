package io.github.mirvmir.course.application.service.port.repository;

import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;

public interface CourseLessonRepository {
    CourseLessonInfoResponse getLessonInfo(Long courseLessonId);
}