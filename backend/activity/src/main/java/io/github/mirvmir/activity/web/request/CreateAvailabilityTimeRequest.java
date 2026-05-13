package io.github.mirvmir.activity.web.request;

import java.time.Instant;

public record CreateAvailabilityTimeRequest(
        Instant startAt
) {
}