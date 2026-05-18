package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateAvailabilityTimeRequest(
        @NotNull
        @Future
        Instant startAt,
        @NotNull
        @Future
        Instant endAt
) {
}