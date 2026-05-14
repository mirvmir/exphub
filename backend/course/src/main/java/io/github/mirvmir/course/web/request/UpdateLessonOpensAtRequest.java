package io.github.mirvmir.course.web.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateLessonOpensAtRequest(
        @NotNull
        @Future
        Instant opensAt
) {
}