package io.github.mirvmir.activity.web.response;

import java.time.Instant;

public record GroupActivitySlotResponse(
        Long id,
        Long activityId,
        Instant startTime,
        Instant endTime,
        Integer bookedSeats,
        Integer maxSeats
) {
}
