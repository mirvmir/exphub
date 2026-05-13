package io.github.mirvmir.activity.web.response;

import java.time.Instant;

public record ActivityAvailableSlotResponse(
        Instant startAt,
        Instant endAt
) {
}
