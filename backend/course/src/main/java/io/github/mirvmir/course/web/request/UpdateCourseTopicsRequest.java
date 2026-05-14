package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.parameters.P;

import java.util.Set;

public record UpdateCourseTopicsRequest(
        @NotNull
        @Positive
        Long subjectId,
        @NotNull
        Set<@NotNull @Positive Long> topicIds
) {
}