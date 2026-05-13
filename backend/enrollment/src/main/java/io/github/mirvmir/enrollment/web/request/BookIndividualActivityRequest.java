package io.github.mirvmir.enrollment.web.request;

import java.time.Instant;

public record BookIndividualActivityRequest(
        Long activityTimeId,
        Instant startAt
) {
}
