package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
        @NotBlank
        String title
) {
}