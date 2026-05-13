package io.github.mirvmir.course.application.service.interfaces;

import io.github.mirvmir.course.web.request.RejectCourseRequest;

public interface ModerationCourseService {
    void approve(Long courseId);
    void reject(Long courseId,
                RejectCourseRequest request);
    void block(Long courseId);
}
