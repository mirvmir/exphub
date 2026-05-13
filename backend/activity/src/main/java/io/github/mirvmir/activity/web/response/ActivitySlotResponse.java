package io.github.mirvmir.activity.web.response;

import java.time.Instant;

public record ActivitySlotResponse(
        Long id,
        Long activityId,
        Instant startTime,
        Instant endTime
) {
}
