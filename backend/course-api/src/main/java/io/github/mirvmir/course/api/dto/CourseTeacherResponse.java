package io.github.mirvmir.course.api.dto;

public record CourseTeacherResponse(
        Long courseId,
        Long teacherId
) {
}