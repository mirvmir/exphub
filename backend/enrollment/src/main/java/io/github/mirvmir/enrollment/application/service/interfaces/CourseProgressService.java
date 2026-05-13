package io.github.mirvmir.enrollment.application.service.interfaces;

import io.github.mirvmir.enrollment.web.response.CourseProgressResponse;

public interface CourseProgressService {
    CourseProgressResponse completeLesson(Long courseId,
                                          Long courseLessonId);
}
