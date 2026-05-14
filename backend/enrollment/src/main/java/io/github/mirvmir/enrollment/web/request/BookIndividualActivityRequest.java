package io.github.mirvmir.enrollment.web.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record BookIndividualActivityRequest(
        @NotNull
        @Positive
        Long activityTimeId,
        @Future
        Instant startAt
) {
}
