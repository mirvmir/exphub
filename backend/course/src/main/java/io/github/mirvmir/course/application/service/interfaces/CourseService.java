package io.github.mirvmir.course.application.service.interfaces;

import io.github.mirvmir.course.web.response.CourseInfoResponse;

public interface CourseService {
    CourseInfoResponse getCourse(Long courseId);
}
