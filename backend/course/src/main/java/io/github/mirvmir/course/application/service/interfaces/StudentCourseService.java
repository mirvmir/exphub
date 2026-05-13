package io.github.mirvmir.course.application.service.interfaces;

import io.github.mirvmir.course.web.response.CourseInfoResponse;
import io.github.mirvmir.course.web.response.StudentCourseLessonResponse;
import io.github.mirvmir.course.web.response.StudentCourseModuleResponse;
import io.github.mirvmir.course.web.response.StudentCourseResponse;

import java.util.UUID;

public interface StudentCourseService {
    CourseInfoResponse getCourseDescription(Long courseId);
    StudentCourseResponse getCourse(Long courseId);
    StudentCourseModuleResponse getModule(Long courseId,
                                          UUID stableModuleId);
    StudentCourseLessonResponse getLesson(Long courseId,
                                          UUID stableLessonId);
}
